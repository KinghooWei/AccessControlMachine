package com.xwlab.attendance.logic.model;

public class Body {
    public static class GetInfosBody {
        String service,community,gate,lastUpdateTime;
        public GetInfosBody(String service,String community,String gate,String lastUpdateTime) {
            this.service = service;
            this.community = community;
            this.gate = gate;
            this.lastUpdateTime = lastUpdateTime;
        }
    }
}
