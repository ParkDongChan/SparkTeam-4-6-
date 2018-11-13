package com.sparktalk.hunandchoo.appointmentapplication.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hun on 2018-08-22.
 */

public class ChatModel {

    public Map<String, Boolean> users = new HashMap<>(); // 채팅방의 유저의 uid, destinationUid 를 갖는 해쉬맵
    public Map<String, Comment> comments = new HashMap<>(); // 채팅방의 내용을 갖는 해쉬맵

    public static class Comment{
        public String uid;
        public String destinationUid;
        public String message;
        public Object timestamp;
        public Map<String, Object> readUsers = new HashMap<>(); // 읽은유저 수 체크
    }

}
