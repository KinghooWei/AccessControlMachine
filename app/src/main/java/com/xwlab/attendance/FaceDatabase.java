package com.xwlab.attendance;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.xwlab.util.SharedPreferencesUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;

import static java.lang.Math.sqrt;

public class FaceDatabase {
    private MyDatabaseHelper helper;
    private String logFile;
    private String logDir;
    private final String mtable = "FaceInfo";
    private Vector<double[]> features = new Vector<>();
    private Vector<String> names = new Vector<>();
    private Vector<String> phoneNums = new Vector<>();
    private Vector<String> passwords = new Vector<>();
    private static final String TAG = "FaceDatabase";
    boolean initialedStatus = false;
    //    private SharedPreferences share;
    private SharedPreferencesUtil shpUtil;

    public FaceDatabase(Context context) {
//        share = context.getSharedPreferences("attendance",Context.MODE_PRIVATE);
        shpUtil = new SharedPreferencesUtil();
        // 更新日志的路径
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        logDir = sdDir.toString() + "/attendance/";
        logFile = logDir + "update_log.txt";

        //从sqlite数据库中加载
        helper = new MyDatabaseHelper(context, "faceDataBase.db");
        SQLiteDatabase database = helper.getWritableDatabase();
        Cursor cursor = database.query(mtable, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                String phoneNum = cursor.getString(cursor.getColumnIndex("phoneNum"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String feature = cursor.getString(cursor.getColumnIndex("feature"));
                String password = cursor.getString(cursor.getColumnIndex("password"));
                Logger.i(TAG, "从SQL读取信息 phoneNum: " + phoneNum + " name: " + name);
                Logger.i(TAG, feature);
                insertRAM(name, phoneNum, password, feature);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    /**
     * 更新SQL的用户数据
     */
    private void updateSQL(String name, String phoneNum, String password, String feature) {
        SQLiteDatabase database = helper.getWritableDatabase();
        // 使用android封装的SQL语法
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("feature", feature);
        values.put("phoneNum", phoneNum);
        values.put("password", password);

        // 更新数据库的信息
        int update = database.update(mtable, values, "phoneNum = ?", new String[]{phoneNum});   //返回受影响的行
        Logger.i(TAG, "更新人员 name: " + name + " phoneNum: " + phoneNum);
    }

    /**
     * 更新RAM的用户数据
     */
    private void updateRAM(String name, String phoneNum, String password, String feature) {
        // 遍历内存的信息，修改指定用户信息
        for (int i = 0; i < phoneNums.size(); i++) {
            if (phoneNums.get(i).equals(phoneNum)) {
                //特征字符串切割转换为字符数组
                double[] featureArray;
                featureArray = featureStringToArray(feature);

                features.set(i, featureArray);
                names.set(i, name);
                phoneNums.set(i, phoneNum);
                passwords.set(i, password);
                break;
            }
        }
    }

    /**
     * 向SQL增添新用户信息
     */
    private void insertSQL(String name, String phoneNum, String password, String feature) {
        SQLiteDatabase database = helper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("feature", feature);
        values.put("phoneNum", phoneNum);
        values.put("password", password);
        long insert = database.insert(mtable, null, values);
        Logger.i(TAG, "新增人员 name: " + name + " phoneNum: " + phoneNum);
    }

    /**
     * 向RAM增添新用户信息
     */
    private void insertRAM(String name, String phoneNum, String password, String feature) {
        //特征字符串切割转换为字符数组
        double[] featureArray;
        featureArray = featureStringToArray(feature);

        features.add(featureArray);
        names.add(name);
        phoneNums.add(phoneNum);
        passwords.add(password);
    }


    /**
     * 比对人脸特征
     *
     * @return 返回匹配的人名，比对不成功则返回""
     */
    public Object[] featureCmp(String fstr) {
//        Logger.i(TAG,fstr);
        Logger.i(TAG, Arrays.toString(features.get(0)));
        Object[] objects;
        String fs[] = fstr.split(",");
        if (fs.length != 128) {
            fs = fstr.split(" ");
            if (fs.length != 128) {
                objects = new Object[]{"", "-1"};
                return objects;
            }
        }
        double[] feature = new double[128];
        for (int i = 0; i < 128; i++) {
            feature[i] = Double.parseDouble(fs[i]);
        }
        int index = 0;
        double mSim = 0;
        for (int i = 0; i < features.size(); i++) {
            if (features.get(i) != null) {
                double sim = calculSimilar(feature, features.get(i));
                Log.i(TAG, names.get(i) + String.valueOf(sim));
                if (sim > mSim) {
                    index = i;
                    mSim = sim;     //选出匹配度最高的

                }
            }
        }
        Logger.i(TAG, "mSim is " + mSim);
        if (mSim > 0.6) {
            objects = new Object[]{names.get(index), phoneNums.get(index)};
            return objects;
        } else if (mSim < 0.3) {
            objects = new Object[]{"unknown", null};
            return objects;
        } else {
            objects = new Object[]{"", null};
            return objects;
        }
    }

    /*
    密码比对
     */
    public boolean passwordCmp(String input) {
        Logger.i(TAG, input);
        for (int i = 0; i < passwords.size(); i++) {
            Logger.i(TAG, passwords.get(i) + passwords.get(i).length());
            if (!passwords.get(i).isEmpty() || !passwords.get(i).equals("null")) {
                Logger.i(TAG, i + "not null");
                if (passwords.get(i).equals(input)) {
                    Logger.i(TAG, "success");
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 对比两个特征，字符串格式
     *
     * @return 返回匹配度，如果插入失败返回0
     */
    public double calculSimilar(String f1, String f2) {
        String fs1[] = f1.split(",");
        String fs2[] = f2.split(",");
        if (fs1.length != 128) {
            fs1 = f1.split(" ");
            if (fs1.length != 128) return 0;
        }
        if (fs2.length != 128) {
            fs2 = f2.split(" ");
            if (fs2.length != 128) return 0;
        }
        double[] v1 = new double[128];
        double[] v2 = new double[128];
        for (int i = 0; i < 128; i++) {
            v1[i] = Double.parseDouble(fs1[i]);
            v2[i] = Double.parseDouble(fs2[i]);
        }
        if (v1.length != v2.length || v1.length == 0)
            return 0;
        double ret = 0.0, mod1 = 0.0, mod2 = 0.0;
        for (int i = 0; i != v1.length; ++i) {
            ret += v1[i] * v2[i];
            mod1 += v1[i] * v1[i];
            mod2 += v2[i] * v2[i];
        }
        return ret / sqrt(mod1) / sqrt(mod2);
    }

    /**
     * 添加数据到FaceInfo表中
     *
     * @return 返回新插入的行号，如果插入失败返回-1
     */
//    public long addData(String name, String feature, String personId, String password) {
////        Log.d("thisadd", "addData: ");
//        SQLiteDatabase database = helper.getReadableDatabase();
//        if (insertRAM(name, feature, personId, password)) {
//            // 使用anddroid封装的SQL语法
//            ContentValues values = new ContentValues();
//            values.put("name", name);
//            values.put("feature", feature);
//            values.put("phoneNum", personId);
//            values.put("password", password);
//            long insert = database.insert(mtable, null, values);
//            Logger.i(TAG, "新增人员信息 name: " + name + " phoneNum: " + personId + " password: " + password);
//            return insert;
//        } else return -1;
//    }

    /**
     * 更新内存和SQL的数据
     *
     * @return 返回受影响的行
     */
//    public int updateData(String name, String feature, String phoneNum, String password) {
//        // 遍历内存的信息，修改指定用户信息
//        for (int i = 0; i < phoneNums.size(); i++) {
//            if (phoneNums.get(i).equals(phoneNum)) {
//
//                double[] featureArray;
//                if (feature.isEmpty() || feature.equals("null")) {
//                    featureArray = null;
//                } else {
//                    featureArray = featureStringToArray(feature);
//                }
//                if (featureArray != null && featureArray.length != 128) {
//                    Logger.e(TAG, "人脸特征格式不正确 name: " + name + " phoneNum: " + phoneNum);
//                    return -1;
//                }
//                features.set(i, featureArray);
//                names.set(i, name);
//                passwords.set(i, password);
//                break;
//            }
//        }
//
//        SQLiteDatabase database = helper.getReadableDatabase();
//        // 使用android封装的SQL语法
//        ContentValues values = new ContentValues();
//        values.put("name", name);
//        values.put("feature", feature);
//        values.put("phoneNum", phoneNum);
//        values.put("password", password);
//
//        // 更新数据库的信息
//        int update = database.update(mtable, values, "phoneNum = ?", new String[]{phoneNum.toString()});   //返回受影响的行
//        Logger.i(TAG, "更新人员 name: " + name + " phoneNum: " + phoneNum);
//        return update;
//    }

    /**
     * 根据name删除FaceInfo中的数据
     *
     * @return 返回受影响的行
     */
    public int deleteByName(String name) {
        SQLiteDatabase database = helper.getReadableDatabase();
        // 使用anddroid封装的SQL语法
        int delete = database.delete(mtable, "name = ?", new String[]{name});
        return delete;
    }

    /**
     * PersonId
     *
     * @return 返回受影响的行
     */
    public int deleteByPhoneNum(String phoneNum) {
        SQLiteDatabase database = helper.getReadableDatabase();
        // 使用anddroid封装的SQL语法
        int delete = database.delete(mtable, "phoneNum = ?", new String[]{phoneNum.toString()});
        return delete;
    }

    /**
     * 下载后台数据时，判断SQL中是否存在手机为phoneNum的用户
     *
     * @param phoneNum
     * @return
     */
    public boolean isExistPhoneNum(String phoneNum) {
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query(mtable, null, "phoneNum = ?", new String[]{phoneNum}, null, null, null);
        //            do {
        //                String personName = cursor.getString(cursor.getColumnIndex("name"));
        //                String fstr = cursor.getString(cursor.getColumnIndex("feature"));
        //                Log.i(TAG, "exist peronId: " + phoneNum + " name: " + personName);
        //            } while (cursor.moveToNext());
        return cursor.moveToFirst();
    }

    /**
     * 删除FaceInfo中所有的数据
     *
     * @return 返回受影响的行
     */
    public int deleteAllData() {
        SQLiteDatabase database = helper.getReadableDatabase();
        // 使用anddroid封装的SQL语法
        int delete = database.delete(mtable, null, new String[]{});

        File file = new File(logFile);
        if (file.exists()) {
            boolean res = file.delete();
        }
        features.clear();
        names.clear();
        phoneNums.clear();
        passwords.clear();
        return delete;
    }

    /**
     * 根据name查询FaceInfo表中的数据
     *
     * @return 返回的是name对应的feature字符串
     */
    public String query(String name) {
        SQLiteDatabase database = helper.getReadableDatabase();
        Cursor cursor = database.query(mtable, null, "name = ?", new String[]{name}, null, null, null);
        String res = "";
        if (cursor.moveToFirst()) {
            do {
                int person_id = cursor.getInt(cursor.getColumnIndex("person_id"));
                String person_name = cursor.getString(cursor.getColumnIndex("name"));
                String feature = cursor.getString(cursor.getColumnIndex("feature"));
                Log.i(TAG, "person_id: " + person_id);
                Log.i(TAG, "name: " + person_name);
                Log.i(TAG, "feature: " + feature);
                res = feature;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return res;
    }

    //特征描述字符串转double数组
    private double[] featureStringToArray(String feature) {

        if (feature.isEmpty() || feature.equals("null")) {
            return null;
        } else {
            Logger.i(TAG, "开始分割转换：" + feature);
            String[] fs = feature.split(" ");        //按空格分割

            double[] featureArray = new double[128];
            for (int i = 0; i < 128; i++) {
                featureArray[i] = Double.parseDouble(fs[i]);
            }
            return featureArray;
        }
    }


    /**
     * 特征匹配
     * 根据加载name和feature到内存中，方便比对
     */
    public double calculSimilar(double[] v1, double[] v2) {
        if (v1.length != v2.length || v1.length == 0)
            return 0;
        double ret = 0.0, mod1 = 0.0, mod2 = 0.0;
        for (int i = 0; i != v1.length; ++i) {
            ret += v1[i] * v2[i];
            mod1 += v1[i] * v1[i];
            mod2 += v2[i] * v2[i];
        }
        return ret / sqrt(mod1) / sqrt(mod2);
    }


    /*
     * 定义文件保存的方法，写入到文件中，所以是输出流
     * */
    public String getUpdataTime() {
        FileInputStream fis = null;
        String result = "";
        try {
            /* 判断sd的外部设置状态是否可以读写 */
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(logFile);
                if (!file.exists()) {
                    return result;
                }
                fis = new FileInputStream(file);
                byte[] buffer = new byte[fis.available()];
                fis.read(buffer);
                fis.close();
                result = new String(buffer, "UTF-8");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /*
     * 定义文件保存的方法，写入到文件中，所以是输出流
     * */
    public void writeUpdateTime(String content) {
        FileOutputStream fos = null;
        try {
            /* 判断sd的外部设置状态是否可以读写 */
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File dir = new File(logDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(logFile);
                // 先清空内容再写入
                fos = new FileOutputStream(file);
                byte[] buffer = content.getBytes();
                fos.write(buffer);
                fos.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void shareWrite(String lastUpdateTime) {

    }

    public boolean loadCsv(String path) {
        // 通过本函数加载的人脸数据为测试数据，不添加到Database中
        Log.i(TAG, "加载本地csv文件: " + path);
        File file = new File(path);
        FileInputStream fileInputStream;
        Scanner in;
        try {
            fileInputStream = new FileInputStream(file);
            in = new Scanner(fileInputStream, "GBK");
            Log.i(TAG, "line: " + in.nextLine());
            int i = 0;
            int j = 0;
            while (in.hasNextLine()) {
                String line = in.nextLine();
//                Log.i(TAG,"line: "+ line);
                i = 0;
                j = line.indexOf(',');
                String name = line.substring(i, j);
                i = j + 1;
                j = line.indexOf(',', i + 1);
                String id = line.substring(i, j);
                i = j + 2;
                j = line.length();
                String fstr = line.substring(i, j - 1);
                // csv文件的人员personId默认为-1
//                if (!flush(name, fstr, "-1")) {
//                    Log.e("FaceDatebase", "loading csv error");
//                    return false;
//                }
                Log.i(TAG, "从本地csv文件成功录入 name: " + name + " person_id: " + "-1");
            }
            Log.i("FaceDatebase", "csv loaded");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    // 从后台(云端)获取人脸数据库
    public class Updatemysql implements Runnable {
        @Override
        public void run() {
            Logger.i(TAG, "start->加载数据库");
            JSONObject jsonObject = new JSONObject();
            String timestamp = HttpUtils.getSecondTimestamp();   //获取当前的时间戳
            try {
                //*** 公共参数 ***//
                jsonObject.put("apiVersion", "1.0");
                jsonObject.put("charset", "UTF-8");
                jsonObject.put("productCode", "DOOR");
                jsonObject.put("system", "ANDROID");
                jsonObject.put("timestamp", timestamp);
                //******  人脸信息接口 ******//
                jsonObject.put("service", "door.person.getAll");
                jsonObject.put("community", shpUtil.loadString("community"));
                jsonObject.put("gate", shpUtil.loadString("gate"));
                jsonObject.put("lastUpdateTime", shpUtil.loadString("lastUpdateTime", "2020-02-02 00:00:00"));
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            String result = HttpUtils.sendJsonPost(jsonObject.toString());

            Logger.i(TAG, "result is " + result);
            int addCount = 0;
            int updateCount = 0;
            try {
                JSONObject jsonObj = new JSONObject(result);
                int resultCode = jsonObj.getInt("resultCode");
                if (resultCode == -1) {
                    Logger.i(TAG, "resultCode is -1");
                    JSONArray personInfos = (JSONArray) jsonObj.get("personInfos");
                    Logger.i(TAG, "personInfos are " + personInfos);
                    for (int i = 0; i < personInfos.length(); i++) {
                        String personInfo = (String) personInfos.get(i);
                        String[] infos = personInfo.split(",");
                        String phoneNum = infos[0];
                        String name = infos[1];
                        String password = infos[2];
                        int start = personInfo.indexOf('[');
                        int end = personInfo.indexOf(']');
                        String feature = personInfo.substring(start + 1, end);
                        if (TextUtils.isEmpty(feature) || feature.equals("null")) {
                            feature = null;
                        } else {
                            //判断人脸特征格式是否正确
                            String[] fs = feature.split(" ");
                            if (fs.length != 128) {
                                Logger.i(TAG, "人脸特征格式不正确，姓名：" + name + "，手机：" + phoneNum + "，特征：" + feature + "，未录入");
                                feature = null;
                            }
                        }
                        if (isExistPhoneNum(phoneNum)) {    //更新信息
                            updateSQL(name, phoneNum, password, feature);
                            updateRAM(name, phoneNum, password, feature);
                            updateCount++;
                        } else {        //增添信息
                            // 增加新的personId数据
                            insertSQL(name, phoneNum, password, feature);
                            insertRAM(name, phoneNum, password, feature);
                            addCount++;
                        }
                    }
                    shpUtil.saveString("lastUpdateTime", timestamp);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            Logger.i("TAG", "新增人员数：" + addCount + " || 更新人员数：" + updateCount);
            initialedStatus = true;
        }
    }

    public void updateDatabase() {
        new Thread(new Updatemysql()).start();
    }

    public boolean isInitialed() {
        return initialedStatus;
    }

}