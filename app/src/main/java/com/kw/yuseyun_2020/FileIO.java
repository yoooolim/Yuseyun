package com.kw.yuseyun_2020;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.os.Bundle;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class FileIO {
    // pilot test 2
    static String directoryName;
    private static  ArrayList<Integer> horIDList = new ArrayList<>(
            Arrays.asList(-1, -2)); // 완전한 가로는 없음
    private static  ArrayList<Integer> verIDList = new ArrayList<>(
            Arrays.asList(-1, -2)); // 완전한 세로도 없음
    private static  ArrayList<Integer> diaIDList = new ArrayList<>( // 대각선만 있음.
            Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                    21, 22, 23, 24, 25, 26, 27, 28, 29, 30,
                    31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
                    41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
                    51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
                    61, 62, 63, 64, 65, 66, 67, 68, 69, 70,
                    71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
                    81, 82, 83, 84, 85, 86, 87, 88, 89, 90,
                    91, 92, 93, 94, 95, 96, 97, 98, 99, 100,
                    101, 102, 103, 104, 105, 106, 107, 108, 109, 110,
                    111, 112, 113, 114, 115, 116, 117, 118, 119, 120,
                    121, 122, 123, 124, 125, 126, 127, 128, 129, 130,
                    131, 132, 133, 134, 135, 136, 137, 138, 139, 140,
                    141, 142, 143, 144, 145, 146, 147, 148, 149, 150));

    /*public FileIO(String dir) {
        directoryName = dir;
    }*/

    public static void setDir (String dir) {
        directoryName = dir;
    }

    public static void generateRoadNetwork() throws IOException {
        /*=======Node.txt 파일읽어오기 작업========*/
        //파일 객체 생성
        File file1 = new File(directoryName + "/data1/Node.txt");

        System.out.println("경로 출력 : " + file1.getAbsolutePath());

        if (!file1.exists()) {
            System.out.println("파일을 읽지 못함");
            //파일을 읽지 못하는 경우
            //emulator에서 data->data->com.example.map_matching->files에 data1(Node.txt, Link.txt)를 추가해주어야함
        } else {
            System.out.println("파일을 읽음");
        }

        //입력 스트림 생성
        FileReader fileReader1 = new FileReader(file1);
        //BufferedReader 클래스 이용하여 파일 읽어오기

        BufferedReader bufferedReader1 = new BufferedReader(fileReader1);
        //System.out.println("======== Node 정보 =======");
        while (bufferedReader1.ready()) {
            String line = bufferedReader1.readLine();
            String[] lineArray = line.split("\t");
            Point coordinate = new Point(lineArray[2], lineArray[1]);// 위도(y), 경도(x) 순서로 저장되어있으므로 순서 바꿈!
            Node node = new Node(lineArray[0], coordinate); // 노드생성
            RoadNetwork.nodeArrayList.add(node); // nodeArrayList에 생성한 노드 추가
            //System.out.println(node); //node 정보 출력
        }
        // close the bufferedReader
        bufferedReader1.close();

        /*=======Link.txt 파일읽어오기 작업========*/
        //파일 객체 생성
        File file2 = new File(directoryName + "/data1/Link.txt");
        //입력 스트림 생성
        FileReader fileReader2 = new FileReader(file2);
        //BufferedReader 클래스 이용하여 파일 읽어오기
        BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
        //System.out.println("======== Link 정보 =======");
        while (bufferedReader2.ready()) {
            String line = bufferedReader2.readLine();
            String[] lineArray = line.split("\t");
            //Point coordinate = new Point (lineArray[1], lineArray[2]);
            // weight 구하기 - 피타고라스법칙 적용
            // a=밑변 b=높이 weight=(a제곱+b제곱)의 제곱근
            Double weight = Calculation.calDistance(
                    RoadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[1])).getCoordinate().getY(),
                    RoadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[1])).getCoordinate().getX(),
                    RoadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[2])).getCoordinate().getY(),
                    RoadNetwork.nodeArrayList.get(Integer.parseInt(lineArray[2])).getCoordinate().getX()
            );

            // link 생성
            Link link = new Link(lineArray[0], lineArray[1], lineArray[2], weight);
            if (horIDList.contains(link.getLinkID())) {
                link.setItLooksLike("hor");
            } else if (diaIDList.contains(link.getLinkID())) {
                link.setItLooksLike("dia");
            } else if (verIDList.contains(link.getLinkID())) {
                link.setItLooksLike("ver");
            } else {
                link.setItLooksLike("꽝입니다");
            }
            RoadNetwork.linkArrayList.add(link); // linkArrayList에 생성한 노드 추가
            //System.out.println(link);  //link 정보 출력
//            System.out.print("involving points:");
//            System.out.println(link.getInvolvingPointList());
        }
        // close the bufferedReader
        bufferedReader2.close();

        /*=======POI.txt 파일읽어오기 작업========*/
        //파일 객체 생성
        File file3 = new File(directoryName + "/data1/POI.txt");

        System.out.println("경로 출력 : " + file1.getAbsolutePath());

        if (!file1.exists()) {
            System.out.println("파일을 읽지 못함");
            //파일을 읽지 못하는 경우
            //emulator에서 data->data->com.example.map_matching->files에 data1(Node.txt, Link.txt)를 추가해주어야함
        } else {
            System.out.println("파일을 읽음");
        }

        //입력 스트림 생성
        FileReader fileReader3 = new FileReader(file3);
        //BufferedReader 클래스 이용하여 파일 읽어오기


        BufferedReader bufferedReader3 = new BufferedReader(fileReader3);
        //System.out.println("======== POI 정보 =======");
        while (bufferedReader3.ready()) {
            String line = bufferedReader3.readLine();
            String[] lineArray = line.split("\t");
            Point coordinate = new Point(lineArray[2], lineArray[1]);// 위도(y), 경도(x) 순서로 저장되어있으므로 순서 바꿈!
            if (lineArray[4].equals("-1")) { // [ID 위도 경도 name -1 관련노드ID한개] 로 구성된 POI
                POI poi = new POI(lineArray[0], coordinate, lineArray[3], lineArray[5]); // POI생성
                RoadNetwork.poiArrayList.add(poi); // nodeArrayList에 생성한 노드 추가
                System.out.println(poi); //poi 정보 출력
                continue;
            }
            // 일반적인 경우: [ID 위도 경도 name 끝노드1 가운데노드 끝노드2] 로 구성된 POI
            POI poi = new POI(lineArray[0], coordinate, lineArray[3], lineArray[4],lineArray[5],lineArray[6]); // POI생성
            RoadNetwork.poiArrayList.add(poi); // nodeArrayList에 생성한 노드 추가
            System.out.println(poi); //poi 정보 출력
        }
        // close the bufferedReader
        bufferedReader1.close();
    }

}