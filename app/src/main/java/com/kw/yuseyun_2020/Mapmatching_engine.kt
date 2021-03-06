package com.kw.yuseyun_2020

import android.graphics.Color
import android.util.Pair
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.MarkerIcons
import kotlin.properties.Delegates

class Mapmatching_engine(naverMap: NaverMap) {

    private lateinit var naverMap: NaverMap
    private val emission: Emission = Emission()
    private val transition:Transition = Transition()
    private val wSize = 3 //윈도사이즈는 3!!!!!!
    var route_length = 0

    fun for_route(naverMap: NaverMap, dir:String, start : Int, end : Int): ArrayList<Int> {

        // Link와 Node를 바탕으로 Adjacent List 구축
        val heads: ArrayList<AdjacentNode> = ArrayList()
        for (i in RoadNetwork.nodeArrayList.indices) {
            val headNode = AdjacentNode(RoadNetwork.nodeArrayList[i])
            heads.add(headNode)
            val adjacentLink: MutableList<Pair<Link, Int>>? =
                    RoadNetwork.getLink1(headNode.node.nodeID) //mutableList?
            if (adjacentLink != null) { //안전하게 하기 위함
                if (adjacentLink.size == 0) continue
            }
            var ptr = headNode
            if (adjacentLink != null) { //안전하게 하기 위함
                for (j in adjacentLink.indices) {
                    val addNode = AdjacentNode(
                            RoadNetwork.getNode(adjacentLink[j].second), adjacentLink[j].first
                    )
                    ptr.nextNode = addNode
                    ptr = ptr.nextNode
                }
            }
        }
        val routeObject = RouteFinding();
        val route : ArrayList<Int>
        route = routeObject.dijkstra(heads,start,end);
        route_length = routeObject.routeLength
        return route
    }

    fun engine(naverMap: NaverMap, dir: String){

        System.out.println("===== [YSY] Map-matching real time =====")

        val testNo = 1 // 여기만 바꿔주면 됨 (PilotTest 2는 data 1만 존재)

        // Link와 Node를 바탕으로 Adjacent List 구축
        val heads: ArrayList<AdjacentNode> = ArrayList()
        for (i in RoadNetwork.nodeArrayList.indices) {
            val headNode = AdjacentNode(RoadNetwork.nodeArrayList[i])
            heads.add(headNode)
            val adjacentLink: MutableList<Pair<Link, Int>>? =
                    RoadNetwork.getLink1(headNode.node.nodeID) //mutableList?
            if (adjacentLink != null) { //안전하게 하기 위함
                if (adjacentLink.size == 0) continue
            }
            var ptr = headNode
            if (adjacentLink != null) { //안전하게 하기 위함
                for (j in adjacentLink.indices) {
                    val addNode = AdjacentNode(
                            RoadNetwork.getNode(adjacentLink[j].second), adjacentLink[j].first
                    )
                    ptr.nextNode = addNode
                    ptr = ptr.nextNode
                }
            }
        }
        //신기한 사실 = get,set 함수를 불러오지 않아도 알아서 척척박사님 알아맞춰보세요
        //여기까지 도로네트워크 생성

        //getNodePrint(naverMap)

        // GPS points와 routePoints를 저장할 ArrayList생성
        val gpsPointArrayList: ArrayList<GPSPoint> = ArrayList()
        val routePointArrayList: ArrayList<Point> // 실제 경로의 points!
        val subMatching: ArrayList<Candidate> = ArrayList()

        // test 번호에 맞는 routePoints생성

        routePointArrayList = RoadNetwork.routePoints(RoadNetwork.routeNodeArrayList)

        // window size만큼의 t-window, ... , t-1, t에서의 candidates의 arrayList
        val arrOfCandidates: ArrayList<ArrayList<Candidate>> = ArrayList()
        val subGPSs: ArrayList<GPSPoint> = ArrayList()

        var timestamp = 0

        // 1: 원래 하던대로 (표준편차 4)  | 2: x혹은 y좌표만 uniform하게(hor, ver, dia에 따라서)
        // 3: x, y 모두 uniform하게     | 4: 교수님이 말한 평균 4 방식
        val gpsGenMode = 2
        println("Fixed Sliding Window Viterbi (window size: 3)")
        for (i in routePointArrayList.indices step (5)) {
            var point: Point = routePointArrayList.get(i)
            //println("routePoint: " + point)
            printPoint(point, Color.YELLOW, naverMap)
        }

        var crossroad_check = 0

        ////////////////////반복문 - gps 생성////////////////////////////////
        for (i in routePointArrayList.indices step (5)) {
            // 오래 걸리는 작업 수행부분
            var point: Point = routePointArrayList.get(i)
            val gpsPoint = GPSPoint(
                    timestamp,
                    point,
                    gpsGenMode,
                    3,
                    RoadNetwork.getLink(point.linkID).itLooksLike
            )
            printPoint(gpsPoint.point, Color.RED, naverMap) // 생성된 GPS출력(빨간색)
            //println("[MAIN] GPS: $gpsPoint")
            gpsPointArrayList.add(gpsPoint)
            timestamp++ //gps 생성후 바로 증가

            val candidates: ArrayList<Candidate> = ArrayList()
            candidates.addAll(
                    Candidate.findRadiusCandidate(
                            gpsPoint.point, 50, timestamp, emission
                    )
            )

            ///////////// FSW VITERBI /////////////
            subGPSs.add(gpsPoint)
            arrOfCandidates.add(candidates)

            ///////////////////matching 진행하는 부분분//////////////////
            //처음 부분 3번은 제일 가까운 candidate에 매칭 (ep)

            if (timestamp <= 3) {
                // 마지막 candidates 중 prob가 가장 높은 것 max_last_candi에 저장
                var max_last_candi = Candidate()
                var max_prob = 0.0
                for (candidate in candidates) {
                    if (max_prob < candidate.ep) {
                        max_prob = candidate.ep
                        max_last_candi = candidate
                    }
                }
                FSWViterbi.setMatched_sjtp(max_last_candi) //가장 ep가 높은 candidate 매칭

                subMatching.add(max_last_candi)
                printMatched(subMatching, Color.GREEN, 50, naverMap) // 매칭: 초록색
                subMatching.clear()

                Emission.Emission_Median(FSWViterbi.getMatched_sjtp().get(timestamp - 1))
                //매칭된 candidate의 median값 저장

                if (timestamp != 1) { //matching 두 개 이상일때부터 median 저장가능
                    var tp = 0.0;
                    tp = Transition.Transition_pro(
                            subGPSs[timestamp - 2].point,
                            subGPSs[timestamp - 1].point,
                            FSWViterbi.getMatched_sjtp().get(timestamp - 2),
                            FSWViterbi.getMatched_sjtp().get(timestamp - 1)

                    )
                    FSWViterbi.getMatched_sjtp().get(timestamp - 2).setTp(tp)

                    Transition.Transition_Median(
                            FSWViterbi.getMatched_sjtp().get(timestamp - 2)
                    ) //매칭된 candidate와 그 전 매칭된 tp의 median값 저장
                    if (timestamp == 3) {
                        subGPSs.clear()
                        arrOfCandidates.clear();
                    }
                }
                continue
            }
            //처음 3번 구하는 부분 끝, 처음 매칭 3번은 비터비 적용x


            if(crossroad_check == 1){
                if(subGPSs.size == 5){ // 5초후까지 확인
                    //갈림길 알고리즘 시작
                    Crossroad.future_gps(arrOfCandidates, subMatching)
                    crossroad_check = 0
                    printMatched(subMatching, Color.GREEN, 50, naverMap) // 세정 매칭: 초록색
                    subMatching.clear()
                    subGPSs.clear()
                    arrOfCandidates.clear()
                    //clear
                    subGPSs.add(gpsPoint)
                    arrOfCandidates.add(candidates)
                    //마지막 gps, candidate 추가
                }
            }
            else {
                if (subGPSs.size == wSize) {
                    //println("===== VITERBI start ====")

                    //println("----- sjtp ------")
                    FSWViterbi.generateMatched(
                            wSize,
                            arrOfCandidates,
                            gpsPointArrayList,
                            timestamp,
                            subMatching
                    )
                    subGPSs.clear()
                    arrOfCandidates.clear()
                    //clear
                    subGPSs.add(gpsPoint)
                    arrOfCandidates.add(candidates)
                    //마지막 gps, candidate 추가

                    //println("===== VITERBI end ====")

                    //갈림길이 있는지 판단
                    //비터비 사이즈 3일때만 가능
                    var m_size = FSWViterbi.getMatched_sjtp().size
                    //
                    if (Crossroad.different_Link(FSWViterbi.getMatched_sjtp()[m_size - 2], FSWViterbi.getMatched_sjtp()[m_size - 3]) == 1) {
                        subMatching.clear() //sub 삭제
                        Crossroad.different_link_matching(FSWViterbi.getMatched_sjtp()[m_size - 2], FSWViterbi.getMatched_sjtp()[m_size - 3], subMatching)
                        //갈림길 가운데 노드로 매칭
                        FSWViterbi.getMatched_sjtp().removeAt(FSWViterbi.getMatched_sjtp().size - 3) //다른 링크로 매칭 x
                        FSWViterbi.getMatched_sjtp().removeAt(FSWViterbi.getMatched_sjtp().size - 2) //다른 링크로 매칭 x
                        crossroad_check = 1;
                    }
                    //
                    else if (Crossroad.different_Link( FSWViterbi.getMatched_sjtp()[m_size - 1], FSWViterbi.getMatched_sjtp()[m_size - 2]) == 1) {
                        subMatching.removeAt(subMatching.size - 1)
                        Crossroad.different_link_matching(FSWViterbi.getMatched_sjtp()[m_size - 1], FSWViterbi.getMatched_sjtp()[m_size - 2], subMatching) //갈림길 가운데 노드로 매칭
                        FSWViterbi.getMatched_sjtp().removeAt(FSWViterbi.getMatched_sjtp().size - 2) //다른 링크로 매칭 x
                        crossroad_check = 1;
                    }

                    printMatched(subMatching, Color.GREEN, 50, naverMap) // 세정 매칭: 초록색
                    subMatching.clear()
                }
            }
            //비터비 끝//
        }
        //printMatched(FSWViterbi.getMatched_sjtp(), Color.GREEN, 50, naverMap) // 세정 매칭: 초록색


    }

    //point 출력하는 함수
    fun printPoint(point: Point, COLOR: Int, naverMap: NaverMap) {
        val marker = Marker() //좌표
        marker.position = LatLng(
                point.y,
                point.x
        )
        marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
        marker.iconTintColor = COLOR //색 덧입히기
        marker.width = 30
        marker.height = 30
        // 마커가 너무 커서 크기 지정해줌
        marker.map = naverMap //navermap에 출력
        var cameraUpdate = CameraUpdate.scrollAndZoomTo(
                LatLng(
                        point.y,
                        point.x
                ), 17.0
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
    }

    fun printMatched(matched: ArrayList<Candidate>, COLOR: Int, SIZE: Int, naverMap: NaverMap) {
        for (i in matched.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                    matched.get(i).point.y,
                    matched.get(i).point.x
            ) //node 좌표 출력
            marker.width = 30
            marker.height = 30
            marker.icon = OverlayImage.fromResource(R.drawable.matching)
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollAndZoomTo(
                LatLng(
                        matched.get(0).point.y,
                        matched.get(0).point.x
                ), 18.0
        )
        naverMap.moveCamera(cameraUpdate)

        //카메라 이동
    }

    fun getNodePrint(naverMap: NaverMap) {
        for (i in RoadNetwork.nodeArrayList.indices) { //indices 또는 index사용
            val marker = Marker() //좌표
            marker.position = LatLng(
                    RoadNetwork.getNode(i).coordinate.y,
                    RoadNetwork.getNode(i).coordinate.x
            ) //node 좌표 출력
            marker.icon = MarkerIcons.BLACK //색을 선명하게 하기 위해 해줌
            marker.iconTintColor = Color.BLUE //색 덧입히기
            marker.width = 30
            marker.height = 50
            // 마커가 너무 커서 크기 지정해줌
            marker.map = naverMap //navermap에 출력
        } //모든 노드 출력

        var cameraUpdate = CameraUpdate.scrollTo(
                LatLng(
                        RoadNetwork.getNode(0).coordinate.x, RoadNetwork.getNode(
                        0
                ).coordinate.y
                )
        )
        naverMap.moveCamera(cameraUpdate)
        //카메라 이동
    }


}