package com.kw.yuseyun_2020;


import android.util.Pair;

import com.naver.maps.map.NaverMap;

import java.util.ArrayList;
import java.util.List;


public class RoadNetwork {

    // 데이터를 보관할 ArrayList들을 담는 class.. >> 그냥 public으로 만들었다!
    public static ArrayList<Node> nodeArrayList = new ArrayList<>();
    public static ArrayList<Link> linkArrayList = new ArrayList<>();
    public static ArrayList<POI> poiArrayList = new ArrayList<>();

    public static ArrayList<Point> routePointArrayList = new ArrayList<>();
    public static ArrayList<Node> routeNodeArrayList = new ArrayList<>(); //경로 저장

    public static String startPOI="유네닭갈비*";
    public static String endPOI="한아름빌라";

    public static void setStartPOI(String startPOI) {
        RoadNetwork.startPOI = startPOI;
    }

    public static void setEndPOI(String endPOI) {
        RoadNetwork.endPOI = endPOI;
    }

    // _nodeID를 nodeID로 가지는 node반환
    public static Node getNode (int _nodeID) {
        for (Node currNode : nodeArrayList) {
            if (currNode.getNodeID() == _nodeID) {
                return currNode;
            }
        }
        // 탐색에 실패한 경우 nodeId가 -1인 Node반환
        return new Node(-1, new Point((double)-99,(double)-99));
    }

    public static Node getNode1 (Point nodePoint){
        for(Node currNode : nodeArrayList){
            if(currNode.getCoordinate().getX().doubleValue() == nodePoint.getX().doubleValue()
                    && currNode.getCoordinate().getY().doubleValue() == nodePoint.getY().doubleValue())
                return currNode;
        }
        // 탐색에 실패한 경우 nodeId가 -1인 Node반환
        return new Node(-1, new Point((double)-99,(double)-99));
    }

    // _linkID를 linkID로 가지는 link반환
    public static Link getLink (int _linkID) {
        for (Link currLink : linkArrayList) {
            if (currLink.getLinkID() == _linkID) {
                return currLink;
            }
        }
        // 탐색에 실패한 경우 nodeId가 -1인 Link반환
        return new Link(-1,-1,-1,(double)-1);
    }
    // nodeID_s를 start node ID로, nodeID_e를 end node id로 가지는 가지는 link반환
    // 혹은 nodeID_e를 start node ID로, nodeID_s를 end node id로 가지는 가지는 link반환
    public static Link getLink (int nodeID_s, int nodeID_e) {
        for (Link currLink : linkArrayList) {
            if ((currLink.getStartNodeID() == nodeID_s) && (currLink.getEndNodeID() == nodeID_e)
                    || (currLink.getStartNodeID() == nodeID_e) && (currLink.getEndNodeID() == nodeID_s)) {
                return currLink;
            }
        }
        // 탐색에 실패한 경우 nodeId가 -1인 Link반환
        return new Link(-1,-1,-1,(double)-1);
    }
    public static List<Pair<Link,Integer>>  getLink1 (int nodeID) {
        List<Pair<Link,Integer>> pairs = new ArrayList<>();
        for (Link currLink : linkArrayList) {
            if (currLink.getStartNodeID() == nodeID) {
                pairs.add(new Pair<Link,Integer>(currLink,currLink.getEndNodeID()));
            }
            else if(currLink.getEndNodeID() == nodeID){
                pairs.add(new Pair<Link,Integer>(currLink,currLink.getStartNodeID()));
            }
        }
        return pairs;
    }

    // POI찾으면 POIID, 못찾으면 -1 return
    public static int getPOIID (int start, int center, int end) {
        for (POI poi : poiArrayList) {

            if (poi.getNode_mid() == center) {

                // 특수한 경우 (코너가 하나가 아닌)
                if (poi.getNode_end1() == -1) {
                    return poi.getPOIID();
                }

                // 일반적인 경우 (코너가 하나인)
                else {
                    // start와 center를 통해 POI ID를 빼 주고 싶었던 경우 << TBT에서 예외처리 때문에 발생한 경우
                    if(end == -1) {
                        if (start == poi.getNode_end1() || start == poi.getNode_end2()) {
                            return poi.getPOIID();
                        }
                    }
                    // 그렇지 않은 일반적인 경우
                    else if ((start == poi.getNode_end1() && end == poi.getNode_end2())
                            || (end == poi.getNode_end1() && start == poi.getNode_end2())
                    ) {
                        return poi.getPOIID();
                    }
                }
            }
        }
        return -1;
    }

    // POI찾으면 POIID, 못찾으면 모든 멤버가 null 혹은 -1로 초기화된 POI반환
    public static POI getPOI (int poiid) {
        for (POI poi : poiArrayList) {
            if (poi.getPOIID() == poiid) {
                return poi;
            }
        }
        return new POI ();
    }

    // poi 이름 받아서 해당 poi와 가장 가까운 node id 반환
    public static int getNodeIDByPoiName (String poiname) {
        for (POI poi : poiArrayList) {
            if (poi.getName().equals(poiname)) {
                return poi.getNode_mid();
            }
        }
        return -1;
    }

    // poi 이름 받아서 해당 poi와 가장 가까운 node id 반환
    public static int getPOIIDByPoiName (String poiname) {
        for (POI poi : poiArrayList) {
            if (poi.getName().equals(poiname)) {
                return poi.getPOIID();
            }
        }
        return -1;
    }

    // testNo에 맞게 경로 Point로 생성하는 작업
    // 아직  startNode가 닿는지 endNode가 닿는지에 따라 순서대로/역순으로 나오는 로직은 추가 안함
    /*왼쪽에서 오른쪽으로 가는 방향만 고려함 (왼, 오를 따질 수 없는 경우는 아래에서 위로 가는 방향만 고려)
     *되는 루트 →, ↑, ↗,↘
     *안되는 루트: ←, ↓, ↙, ↖
     * */
    /*
    public static ArrayList<Point> routePoints (int testNo) {
        ArrayList<Point> routePoints = new ArrayList<>();

        if(testNo == 1){
            int[] routeNodes = { 0, 10, 7, 9, 15, 14*//*, 27, 50, 48, 40, 47, 46, 45, 58 *//*};
            for (int i=0; i<routeNodes.length-1; i++) {
                routeNodeArrayList.add(getNode(routeNodes[i])); /// 새로 추가!
                Link routelink = getLink(routeNodes[i], routeNodes[i+1]); //두 노드를 끝으로 하는 링크 반환
                routePoints.addAll(getInvolvingPointList(getNode(routeNodes[i]).getCoordinate(),
                        getNode(routeNodes[i+1]).getCoordinate(), routelink.getWeight()));

                *//*
                routePoints.addAll(getInvolvingPointList(getNode(routelink.getStartNodeID()).getCoordinate(),
                        getNode(routelink.getEndNodeID()).getCoordinate()));*//*
            }
            routeNodeArrayList.add(getNode(routeNodes[routeNodes.length-1])); // 새로추가!
        } else if(testNo == 2){
            int[] routeNodes = { 1, 2, 3, 4, 65 };
            for (int i=0; i<routeNodes.length-1; i++) {
                routeNodeArrayList.add(getNode(routeNodes[i])); /// 새로 추가!
                Link routelink = getLink(routeNodes[i], routeNodes[i+1]); //두 노드를 끝으로 하는 링크 반환
                routePoints.addAll(getInvolvingPointList(getNode(routeNodes[i]).getCoordinate(),
                        getNode(routeNodes[i+1]).getCoordinate(), routelink.getWeight()));

                *//*
                routePoints.addAll(getInvolvingPointList(getNode(routelink.getStartNodeID()).getCoordinate(),
                        getNode(routelink.getEndNodeID()).getCoordinate()));*//*
            }

            routeNodeArrayList.add(getNode(routeNodes[routeNodes.length-1])); // 새로 추가!
        } else if(testNo == 3){
            int[] routeNodes = { 9, 6, 5, 59, 60 };
            for (int i=0; i<routeNodes.length-1; i++) {
                routeNodeArrayList.add(getNode(routeNodes[i])); ////새로추가!
                Link routelink = getLink(routeNodes[i], routeNodes[i+1]); //두 노드를 끝으로 하는 링크 반환
                routePoints.addAll(getInvolvingPointList(getNode(routeNodes[i]).getCoordinate(),
                        getNode(routeNodes[i+1]).getCoordinate(), routelink.getWeight()));

                *//*
                routePoints.addAll(getInvolvingPointList(getNode(routelink.getStartNodeID()).getCoordinate(),
                        getNode(routelink.getEndNodeID()).getCoordinate()));*//*
            }
            routeNodeArrayList.add(getNode(routeNodes[routeNodes.length-1])); // 새로 추가!
        } else if(testNo == 4){
            int routeStartNode = getNodeIDByPoiName(startPOI);
            int routeEndNode = getNodeIDByPoiName(endPOI);
            int[] routeNodes = {routeStartNode, 65, 4, 5, 6, routeEndNode};
            for (int i=0; i<routeNodes.length-1; i++) {
                routeNodeArrayList.add(getNode(routeNodes[i])); /// 새로 추가!
                Link routelink = getLink(routeNodes[i], routeNodes[i+1]); //두 노드를 끝으로 하는 링크 반환
                routePoints.addAll(getInvolvingPointList(getNode(routeNodes[i]).getCoordinate(),
                        getNode(routeNodes[i+1]).getCoordinate(), routelink.getWeight()));

                *//*
                routePoints.addAll(getInvolvingPointList(getNode(routelink.getStartNodeID()).getCoordinate(),
                        getNode(routelink.getEndNodeID()).getCoordinate()));*//*
            }

            routeNodeArrayList.add(getNode(routeNodes[routeNodes.length-1])); // 새로 추가!
        }
        routePointArrayList = routePoints;

        return routePoints;
    }
    */

    public static ArrayList<Point> routePoints (ArrayList<Node> nodes){
        ArrayList<Point> routePoints = new ArrayList<>();
        int[] routeNodes = new int[nodes.size()];
        int j=0;
        for(Node n : nodes){
            routeNodes[j] = n.getNodeID();
            j++;
        }

        for (int i=0; i<routeNodes.length-1; i++) {
            Link routelink = getLink(routeNodes[i], routeNodes[i+1]); //두 노드를 끝으로 하는 링크 반환
            routePoints.addAll(getInvolvingPointList(getNode(routeNodes[i]).getCoordinate(),
                    getNode(routeNodes[i+1]).getCoordinate(), routelink.getWeight()));
        }

        return routePoints;
    }

    // link개수 출력하기
    int getLinksSize () {
        return linkArrayList.size();
    }

    // 우리 route node만 입력 해도 실제 경로 쭈르륵 떠야 해서 이 부분에 involving point list 살짝 변경해서 넣음
    // GPS데이터 생성을 위한 Point.linkID 설정하는 코드 추가
    public static ArrayList<Point> getInvolvingPointList(Point start, Point end, Double weight){

        //involving points 구하기

        // start point와 end point 좌표 지정
        double xs = start.getX();
        double ys = start.getY();
        double xe = end.getX();
        double ye = end.getY();

        ArrayList<Point> involvingPointList = new ArrayList<>();

        int linkID = getLink(getNode1(new Point(xs, ys)).getNodeID(), getNode1(new Point(xe, ye)).getNodeID()).getLinkID();

        //기울기와 상관없이 구하겠음
        double deltaX = (xe-xs)/(Math.round(weight)); //X 변화값
        double deltaY = (ye-ys)/(Math.round(weight)); //Y 변화값
        //(int)(Math.round(weight)) : weight를 반올림하여 정수로 나타냄

        for(int i = 0; i < (int)(Math.round(weight)); i++) {
            involvingPointList.add(new Point(xs + (i*deltaX), ys + (i*deltaY), linkID));
        } // involvingPointList에 Point 추가

        return involvingPointList;
    }

    public static void printRoadNetwork() {

        System.out.println("[RoadNetwork] print roadNetwork\n----node----");
        for (Node node: nodeArrayList) {
            System.out.println(node);
        }
        System.out.println("----link----");
        for (Link link: linkArrayList) {
            System.out.println(link);
        }
        System.out.println("----poi----");
        for (POI poi: poiArrayList) {
            System.out.println(poi);
        }
    }

    public static ArrayList<Node> getRouteNodeArrayList() {
        return routeNodeArrayList;
    }
}