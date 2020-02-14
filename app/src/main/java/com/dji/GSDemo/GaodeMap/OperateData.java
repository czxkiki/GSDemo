package com.dji.GSDemo.GaodeMap;
import android.os.Handler;
import android.os.Message;
import android.os.Message;
import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dji.common.mission.waypoint.Waypoint;
import dji.ux.c.a;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.nio.file.Paths.get;
import static org.bouncycastle.asn1.x500.style.RFC4519Style.name;

/**
 * @author zhangyan
 * 主要完成 字符串数组转json字符串
 * 以及对json数据的发送和接收
 */
public class OperateData {
    Handler handler;
    String url;

    /**
     * @param stringArray 将string数组转成json格式字符串
     * @return
     */
    //login
    public String stringTojson(String stringArray[]) {
        JSONObject jsonObject = null;
        if (stringArray == null) {
            return "";
        }
        jsonObject = new JSONObject();
        try {
            jsonObject.put("username", stringArray[0]);
            jsonObject.put("password", stringArray[1]);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonString = String.valueOf(jsonObject);
        return jsonString;
    }
    //get waypointlist
    public String namestringTojson(String stringArray[]) {
        JSONObject jsonObject = null;
        if (stringArray == null) {
            return "";
        }
        jsonObject = new JSONObject();
        try {
            jsonObject.put("listname", stringArray[0]);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonString = String.valueOf(jsonObject);
        return jsonString;
    }
    //save
    public String savestringTojson(String stringArray[]) {
        JSONObject jsonObject = null;
        if (stringArray == null) {
            return "";
        }
        jsonObject = new JSONObject();
        try {
            jsonObject.put("username", stringArray[0]);
            jsonObject.put("password", stringArray[1]);
            jsonObject.put("i", stringArray[2]);
            jsonObject.put("count",stringArray[3]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonString = String.valueOf(jsonObject);
        return jsonString;
    }

    /**
     * 功能：json字符串转字符串
     *
     * @param jsonString
     * @return String
     */
    public int jsonToint(String jsonString) {
        int type = 1;
        try {
            JSONObject responseJson = new JSONObject(jsonString);
            type = responseJson.getInt("type");
            Log.i("type", "" + type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return type;
    }

    /**接收前端json
     *
     */
    public List<Map<String, Object>> receiveJson(final android.os.Handler mh, final URL url) {
        List<Map<String, Object>> reData = new ArrayList<>();
        if (url == null) {
            mh.sendEmptyMessage(3);
        } else {
            new Thread(new Runnable() {

                @Override
                public void run() {

                    HttpURLConnection httpURLConnection = null;
                    BufferedReader bufferedReader = null;
                    try {
                        httpURLConnection = (HttpURLConnection) url.openConnection();
                        // 设置连接超时时间
                        httpURLConnection.setConnectTimeout(5 * 1000);
                        //设置从主机读取数据超时
                        httpURLConnection.setReadTimeout(5 * 1000);
                        // Post请求必须设置允许输出 默认false
                        httpURLConnection.setDoOutput(true);
                        //设置请求允许输入 默认是true
                        httpURLConnection.setDoInput(true);
                        // Post请求不能使用缓存
                        httpURLConnection.setUseCaches(false);
                        // 设置为Post请求
                        httpURLConnection.setRequestMethod("POST");
                        //设置本次连接是否自动处理重定向
                        httpURLConnection.setInstanceFollowRedirects(true);
                        // 配置请求Content-Type
                        httpURLConnection.setRequestProperty("Content-Type", "application/json");
                        //开始连接
                        httpURLConnection.connect();


                        //发送数据
//                        Log.i("JSONString", jsonString);
//                        DataOutputStream os = new DataOutputStream(httpURLConnection.getOutputStream());
//                        os.writeBytes(jsonString);
//                        System.out.print(jsonString);
//                        os.flush();
//                        os.close();
//                        Log.i("状态码：", "" + httpURLConnection.getResponseCode());

                        if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String temp;
                            while ((temp = bufferedReader.readLine()) != null) {
                                response.append(temp);
                                System.out.println(temp);
                                System.out.println("------------------------------");

                                String receivestring = response.toString();

                                System.out.println(receivestring.length());


                                try
                                {
                                    JSONArray reJsonArray = (JSONArray) new JSONTokener(receivestring.toString()).nextValue();
                                    for (int i = 0; i < 2; i++)
                                    {
                                        JSONObject object = reJsonArray.getJSONObject(i);
                                        Map<String, Object> item = new TreeMap<String, Object>();
                                        Iterator iterator = object.keys();
                                        while (iterator.hasNext())
                                        {
                                            String key = (String) iterator.next();
                                            Object value = object.get(key);
                                            item.put(key,value);
                                        }
                                        reData.add(item);
                                        System.out.println("+");
                                        System.out.println("\n");
                                        System.out.print(reData);
                                        //todo
                            }

                                }catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }


//                            int type = jsonToint(response.toString());
                            //根据
//                            switch (type) {
//                                case 0:
//                                    mh.sendEmptyMessage(1);
//                                    break;
//                                case 1:
//                                    mh.sendEmptyMessage(2);
//                                    break;
//                                default:
//                            }
                        } else {
                            mh.sendEmptyMessage(0);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mh.sendEmptyMessage(4);
                    } finally {
                        //关闭bufferedreader
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                        }
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                    }
                }
            }).start();
        }
        return reData;
    }
    /**
     * 功能：发送jsonString到服务器并解析回应
     *
     * @param jsonString mh url
     *                   handler 参数规定
     *                   msg.what:
     *                   0：服务器连接失败
     *                   1：注册/登录成功 跳转页面
     *                   2：用户已存在/登录失败
     *                   3：地址为空
     *                   4： 连接超时
     */
    public void sendData(final String jsonString, final android.os.Handler mh, final URL url) {

        if (url == null) {
            mh.sendEmptyMessage(3);
        } else {
            new Thread(new Runnable() {

                @Override
                public void run() {

                    HttpURLConnection httpURLConnection = null;
                    BufferedReader bufferedReader = null;
                    try {
                        httpURLConnection = (HttpURLConnection) url.openConnection();
                        // 设置连接超时时间
                        httpURLConnection.setConnectTimeout(5 * 1000);
                        //设置从主机读取数据超时
                        httpURLConnection.setReadTimeout(5 * 1000);
                        // Post请求必须设置允许输出 默认false
                        httpURLConnection.setDoOutput(true);
                        //设置请求允许输入 默认是true
                        httpURLConnection.setDoInput(true);
                        // Post请求不能使用缓存
                        httpURLConnection.setUseCaches(false);
                        // 设置为Post请求
                        httpURLConnection.setRequestMethod("POST");
                        //设置本次连接是否自动处理重定向
                        httpURLConnection.setInstanceFollowRedirects(true);
                        // 配置请求Content-Type
                        httpURLConnection.setRequestProperty("Content-Type", "application/json");
                        //开始连接
                        httpURLConnection.connect();


                        //发送数据
                        Log.i("JSONString", jsonString);
                        DataOutputStream os = new DataOutputStream(httpURLConnection.getOutputStream());
                        os.writeBytes(jsonString);
                        System.out.print(jsonString);
                        os.flush();
                        os.close();
                        Log.i("状态码：", "" + httpURLConnection.getResponseCode());

                        if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                            StringBuilder response = new StringBuilder();
                            String temp;
                            while ((temp = bufferedReader.readLine()) != null) {
                                response.append(temp);
                                Log.i("Main", response.toString());
                            }
                            int type = jsonToint(response.toString());
                            //根据
                            switch (type) {
                                case 0:
                                    mh.sendEmptyMessage(1);
                                    break;
                                case 1:
                                    mh.sendEmptyMessage(2);
                                    break;
                                default:
                            }
                        } else {
                            mh.sendEmptyMessage(0);
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                        mh.sendEmptyMessage(4);
                    } finally {
                        //关闭bufferedreader
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException e) {
                                e.printStackTrace();

                            }
                        }
                        if (httpURLConnection != null) {
                            httpURLConnection.disconnect();
                        }
                    }
                }
            }).start();
        }
    }

    //**Waypint List加载
    public List<Waypoint> finalListName(ArrayList redata){
        List<Map<String, Object>> LocListMaps = new ArrayList<Map<String, Object>>();
        List<Waypoint> waypoint = new ArrayList<>();

            int i =LocListMaps.size();
            for (int j = 0; j < i ; j++) {
                String a;  //单个list长度
                a = LocListMaps.get(1).get("count").toString();
                int b;
                b = Integer.parseInt(a);
                for (int k = 0; k <= b; k++) {
//                    HashMap<String, Object> listName = new HashMap<>();
//                    String listnameOnly = null;
//                    listName.put(listnameOnly, String.valueOf(LocListMaps.get(3).values()).toString());
                    waypoint.add(0, (Waypoint) LocListMaps.get(2 + k * 4).get("location"));
                break;
                }
                break;
            }

        return waypoint;


}
//*List<Map<String, Object>> 转String
    public String[] jobString(List<Map<String, Object>> reData){
        List<String> nameList = new ArrayList<>();
        List<Map<String, Object>> receiveNameList = new ArrayList<Map<String, Object>>();
        int i =receiveNameList.size();
        for (int j = 0; j < i; j++) {
            nameList.add(receiveNameList.get(j).toString());
        }
        String[] nameString=nameList.toArray(new String[nameList.size()]);
        for(String s:nameString){
            System.out.println(s);
        }
        return nameString;
    }
    private void sendRequsetWithOKHttp(String URL) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    //耗时操作放在新线程
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(URL).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    String[] joblist = responseData.split(":\"(.*?)\"");
//                    TreeMap<String,Object> TreeMaps=stringToJsonObject(responseData);
                    //把数据传出线程
                    Message message=new Message();
                    message.obj=joblist;
                    System.out.println(joblist);
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }


            }
        }).start();
    }
    public void operateData(String url,Handler handler){
        this.url=url;
        this.handler=handler;
        sendRequsetWithOKHttp(url);
    }
}
