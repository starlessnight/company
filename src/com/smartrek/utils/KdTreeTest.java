package com.smartrek.utils;

import java.util.List;

import org.json.JSONObject;

import android.test.AndroidTestCase;
import android.text.format.Time;
import android.util.Log;

import com.smartrek.mappers.RouteMapper;
import com.smartrek.models.Route;
import com.smartrek.utils.KdTree.Node;

public class KdTreeTest extends AndroidTestCase {

    public void test() throws Exception {
        
        String raw = "{\"ROUTE\":[{ \"LATITUDE\":32,\"LONGITUDE\":111,\"NODEID\":1902},{ \"LATITUDE\":33,\"LONGITUDE\":112,\"NODEID\":1899},{ \"LATITUDE\":34,\"LONGITUDE\":113,\"NODEID\":1921},{ \"LATITUDE\":33,\"LONGITUDE\":114,\"NODEID\":1906},{ \"LATITUDE\":34,\"LONGITUDE\":115,\"NODEID\":1905}],\"ESTIMATED_TRAVEL_TIME\":34,\"RID\":\"2336022\"}";
        
        Time departureTime = new Time();
        departureTime.setToNow();
        
        RouteMapper mapper = new RouteMapper();
        Route route = mapper.parseRoute(new JSONObject(raw), departureTime);

        List<RouteNode> nodes = route.getNodes();
//        Node root = KdTree.build(nodes, 0, nodes.size()-1, 0);
//
//        KdTree.print(root, 0);
//        
//        Node q = root.lookup(33, 113.5f, 0);
//        Log.d("KdTreeTest", "Closest node = " + q);
        
        RouteNode cn = NaiveNNS.findClosestNode(nodes, 32, 111.5f);
        Log.d("KdTreeTest", "Closest node = " + cn);
        
        Log.d("KdTreeTest", "Prev node = " + cn.getPrevNode());
        Log.d("KdTreeTest", "Next node = " + cn.getNextNode());
    }

}

