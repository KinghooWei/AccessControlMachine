package com.xwlab.attendance;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.xwlab.util.PermissionUtil;
import com.xwlab.util.SharedPreferencesUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class InitActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final String TAG = "InitActivity";
    private final static int REQUEST = 100;

    private Spinner spProvince, spCity, spCommunity, spGate;
    private String[] provinceArray = {"请选择", "北京市", "天津市", "河北省", "山西省", "内蒙古自治区", "辽宁省",
            "吉林省", "黑龙江省", "上海市", "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省",
            "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区", "海南省", "重庆市", "四川省",
            "贵州省", "云南省", "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "台湾省",
            "香港特别行政区", "澳门特别行政区"};
    private String[] cityArray, communityArray, gateArray;
    private String province, city, community, gate;
    private Button btnOK;

    private SharedPreferencesUtil shareUtil;
    private boolean hasAddress;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
//        CameraManager cm = (CameraManager) getSystemService(CAMERA_SERVICE);
//        try {
//            String[] cl = cm.getCameraIdList();
//            Logger.i("test",cl[0]);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
        shareUtil = new SharedPreferencesUtil(this);
        hasAddress = shareUtil.loadBoolean("hasAddress", false);
        cityArray = new String[]{"请选择"};
        communityArray = new String[]{"请选择"};
        gateArray = new String[]{"请选择"};
        if (PermissionUtil.checkMultiPermission(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.SYSTEM_ALERT_WINDOW}, REQUEST)) {
            initWork();
        }
    }

    private void initUI() {
        initProvince();
        initCity();
        initCommunity();
        initGate();
        btnOK = (Button) findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(this);
    }

    private void initProvince() {
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, R.layout.item_select, provinceArray);
        provinceAdapter.setDropDownViewResource(R.layout.item_dropdown);
        spProvince = (Spinner) findViewById(R.id.sp_province);
        spProvince.setAdapter(provinceAdapter);
        spProvince.setSelection(0);
        spProvince.setOnItemSelectedListener(this);
    }

    private void initCity() {
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, R.layout.item_select, cityArray);
        cityAdapter.setDropDownViewResource(R.layout.item_dropdown);
        spCity = (Spinner) findViewById(R.id.sp_city);
        spCity.setAdapter(cityAdapter);
        spCity.setSelection(0);
        spCity.setOnItemSelectedListener(this);
    }

    private void initCommunity() {
        ArrayAdapter<String> communityAdapter = new ArrayAdapter<>(this, R.layout.item_select, communityArray);
        communityAdapter.setDropDownViewResource(R.layout.item_dropdown);
        spCommunity = (Spinner) findViewById(R.id.sp_community);
        spCommunity.setAdapter(communityAdapter);
        spCommunity.setSelection(0);
        spCommunity.setOnItemSelectedListener(this);
    }

    private void initGate() {
        ArrayAdapter<String> gateAdapter = new ArrayAdapter<>(this, R.layout.item_select, gateArray);
        gateAdapter.setDropDownViewResource(R.layout.item_dropdown);
        spGate = (Spinner) findViewById(R.id.sp_gate);
        spGate.setAdapter(gateAdapter);
        spGate.setSelection(0);
        spGate.setOnItemSelectedListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initWork();
                } else {
                    new AlertDialog.Builder(this).setTitle("提示").setMessage("需先开启相机权限").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    }).create().show();
                }
                break;
        }
    }

    /**
     * 加载人脸识别文件，并跳转至工作界面
     */
    private void initWork() {
        if (hasAddress) {
            try {
                copyBigDataToSD("det1.bin");
                copyBigDataToSD("det2.bin");
                copyBigDataToSD("det3.bin");
                copyBigDataToSD("det1.param");
                copyBigDataToSD("det2.param");
                copyBigDataToSD("det3.param");
                copyBigDataToSD("recognition.bin");
                copyBigDataToSD("recognition.param");
            } catch (IOException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(this, Main3Activity.class);
            startActivity(intent);
            finish();
        } else {
            initUI();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//        Logger.i(TAG, "" + parent.getId());
        switch (parent.getId()) {
            case R.id.sp_province:
                province = provinceArray[position];
                switch (province) {
                    case "北京市":
                        cityArray = new String[]{"请选择", "北京市"};
                        break;
                    case "天津市":
                        cityArray = new String[]{"请选择", "天津市"};
                        break;
                    case "河北省":
                        cityArray = new String[]{"请选择", "石家庄市", "唐山市", "秦皇岛市", "邯郸市", "邢台市", "保定市", "张家口市", "承德市", "沧州市", "廊坊市", "衡水市"};
                        break;
                    case "山西省":
                        cityArray = new String[]{"请选择", "太原市", "大同市", "阳泉市", "长治市", "晋城市", "朔州市", "晋中市", "运城市", "沂州市", "临汾市", "吕梁市"};
                        break;
                    case "内蒙古自治区":
                        cityArray = new String[]{"请选择", "呼和浩特市", "包头市", "乌海市", "赤峰市", "通辽市", "鄂尔多斯市", "呼伦贝尔市", "巴彦淖尔市", "乌兰察布市", "兴安盟", "锡林郭勒盟", "阿拉善盟"};
                        break;
                    case "辽宁省":
                        cityArray = new String[]{"请选择", "沈阳市", "大连市", "鞍山市", "抚顺市", "本溪市", "丹东市", "锦州市", "营口市", "阜新市", "辽阳市", "盘锦市", "铁岭市", "朝阳市", "葫芦岛市"};
                        break;
                    case "吉林省":
                        cityArray = new String[]{"请选择", "长春市", "吉林市", "四平市", "辽源市", "通化市", "白山市", "松原市", "白城市", "延边朝鲜族自治州"};
                        break;
                    case "黑龙江省":
                        cityArray = new String[]{"请选择", "哈尔滨市", "齐齐哈尔市", "鸡西市", "鹤岗市", "双鸭山市", "大庆市", "伊春市", "佳木斯市", "七台河市", "牡丹江市", "黑河市", "绥化市", "大兴安岭地区"};
                        break;
                    case "上海市":
                        cityArray = new String[]{"请选择", "上海市"};
                        break;
                    case "江苏省":
                        cityArray = new String[]{"请选择", "南京市", "无锡市", "徐州市", "常州市", "苏州市", "南通市", "连云港市", "淮安市", "盐城市", "扬州市", "镇江市", "泰州市", "宿迁市"};
                        break;
                    case "浙江省":
                        cityArray = new String[]{"请选择", "杭州市", "宁波市", "温州市", "嘉兴市", "湖州市", "绍兴市", "金华市", "衢州市", "舟山市", "台州市", "丽水市"};
                        break;
                    case "安徽省":
                        cityArray = new String[]{"请选择", "合肥市", "芜湖市", "蚌埠市", "淮南市", "马鞍山市", "淮北市", "铜陵市", "安庆市", "黄山市", "滁州市", "阜阳市", "宿州市", "六安市", "亳州市", "池州市", "宣城市"};
                        break;
                    case "福建省":
                        cityArray = new String[]{"请选择", "福州市", "厦门市", "莆田市", "三明市", "泉州市", "漳州市", "南平市", "龙岩市", "宁德市"};
                        break;
                    case "江西省":
                        cityArray = new String[]{"请选择", "南昌市", "景德镇市", "萍乡市", "九江市", "新余市", "鹰潭市", "赣州市", "吉安市", "宜春市", "抚州市", "上饶市"};
                        break;
                    case "山东省":
                        cityArray = new String[]{"请选择", "济南市", "青岛市", "淄博市", "枣庄市", "东营市", "烟台市", "潍坊市", "济宁市", "泰安市", "威海市", "日照市", "临沂市", "德州市", "聊城市", "滨州市", "菏泽市"};
                        break;
                    case "河南省":
                        cityArray = new String[]{"请选择", "郑州市", "开封市", "洛阳市", "平顶山市", "安阳市", "鹤壁市", "新乡市", "焦作市", "濮阳市", "许昌市", "漯河市", "三门峡市", "南阳市", "商丘市", "信阳市", "周口市", "驻马店市", "省直辖县级行政区划"};
                        break;
                    case "湖北省":
                        cityArray = new String[]{"请选择", "武汉市", "黄石市", "十堰市", "宜昌市", "襄阳市", "鄂州市", "荆门市", "孝感市", "荆州市", "黄冈市", "咸宁市", "随州市", "恩施土家族苗族自治州", "省直辖县级行政区划"};
                        break;
                    case "湖南省":
                        cityArray = new String[]{"请选择", "长沙市", "株洲市", "湘潭市", "衡阳市", "邵阳市", "岳阳市", "常德市", "张家界市", "益阳市", "郴州市", "永州市", "怀化市", "娄底市", "湘西土家族苗族自治州"};
                        break;
                    case "广东省":
                        cityArray = new String[]{"请选择", "广州市", "韶关市", "深圳市", "珠海市", "汕头市", "佛山市", "江门市", "湛江市", "茂名市", "肇庆市", "惠州市", "梅州市", "汕尾市", "河源市", "阳江市", "清远市", "东莞市", "中山市", "潮州市", "揭阳市", "云浮市"};
                        break;
                    case "广西壮族自治区":
                        cityArray = new String[]{"请选择", "南宁市", "柳州市", "桂林市", "梧州市", "北海市", "防城港市", "钦州市", "贵港市", "玉林市", "百色市", "贺州市", "河池市", "来宾市", "崇左市"};
                        break;
                    case "海南省":
                        cityArray = new String[]{"请选择", "海口市", "三亚市", "三沙市", "儋州市", "省直辖县级行政区划"};
                        break;
                    case "重庆市":
                        cityArray = new String[]{"请选择", "重庆市", "县"};
                        break;
                    case "四川省":
                        cityArray = new String[]{"请选择", "成都市", "自贡市", "攀枝花市", "泸州市", "德阳市", "绵阳市", "广元市", "遂宁市", "内江市", "乐山市", "南充市", "眉山市", "宜宾市", "广安市", "达州市", "雅安市", "巴中市", "资阳市", "阿坝藏族羌族自治州", "甘孜藏族自治州", "凉山彝族自治州"};
                        break;
                    case "贵州省":
                        cityArray = new String[]{"请选择", "贵州市", "六盘水市", "遵义市", "安顺市", "毕节市", "铜仁市", "黔西南布依族自治州", "黔东南苗族侗族自治州", "黔南布依族苗族自治州"};
                        break;
                    case "云南省":
                        cityArray = new String[]{"请选择", "昆明市", "曲靖市", "玉溪市", "保山市", "昭通市", "丽江市", "普洱市", "临沧市", "楚雄彝族自治州", "红河哈尼族彝族自治州", "文山壮族苗族自治州", "西双版纳傣族自治州", "大理白族自治州", "德宏傣族景颇族自治州", "怒江傈僳族自治州", "迪庆藏族自治州"};
                        break;
                    case "西藏自治区":
                        cityArray = new String[]{"请选择", "拉萨市", "日喀则市", "昌都市", "林芝市", "山南市", "那曲地区", "阿里地区"};
                        break;
                    case "陕西省":
                        cityArray = new String[]{"请选择", "西安市", "铜川市", "宝鸡市", "咸阳市", "渭南市", "延安市", "汉中市", "榆林市", "安康市", "商洛市"};
                        break;
                    case "甘肃省":
                        cityArray = new String[]{"请选择", "兰州市", "嘉峪关市", "金昌市", "白银市", "天水市", "武威市", "张掖市", "平凉市", "酒泉市", "庆阳市", "定西市", "陇南市", "临夏回族自治州", "河甘南藏族自治州"};
                        break;
                    case "青海省":
                        cityArray = new String[]{"请选择", "西宁市", "海东市", "海北藏族自治州", "黄南藏族自治州", "海南藏族自治州", "果洛藏族自治州", "玉树藏族自治州", "海西蒙古族藏族自治州"};
                        break;
                    case "宁夏回族自治区":
                        cityArray = new String[]{"请选择", "银川市", "石嘴山市", "吴忠市", "固原市", "中卫市"};
                        break;
                    case "新疆维吾尔自治区":
                        cityArray = new String[]{"请选择", "乌鲁木齐市", "克拉玛依市", "吐鲁番市", "哈密市", "昌吉回族自治州", "博尔塔拉蒙古自治州", "巴音郭楞蒙古自治州", "阿克苏地区", "克孜勒苏柯尔克孜自治州", "喀什地区", "和田地区", "伊犁哈萨克自治州", "塔城地区", "阿勒泰", "自治区直辖县级行政区划"};
                        break;
                    case "台湾省":
                        cityArray = new String[]{"请选择", "台北市", "高雄市", "台南市", "台中市", "南投县", "基隆市", "新竹市", "嘉义市", "新北市", "宜兰县", "新竹县", "桃园市", "苗栗县", "彰化县", "嘉义县", "云林县", "屏东县", "台东县", "花莲县", "澎湖县"};
                        break;
                    case "香港特别行政区":
                        cityArray = new String[]{"请选择", "香港特别行政区"};
                        break;
                    case "澳门特别行政区":
                        cityArray = new String[]{"请选择", "澳门特别行政区"};
                        break;
                }
                communityArray = new String[]{"请选择"};
                gateArray = new String[]{"请选择"};
                city = null;
                community = null;
                gate = null;
                initCity();
                initCommunity();
                initGate();
                break;
            case R.id.sp_city:
                city = cityArray[position];
                switch (city) {
                    case "广州市":
                        communityArray = new String[]{"请选择", "豪利花园", "凯旋新世界广粤尊府", "翡翠绿洲森林半岛", "中海康城花园", "云溪四季"};
                        break;
                    case "深圳市":
                        communityArray = new String[]{"请选择", "国展苑", "龙珠花园", "万科清林径", "英郡年华", "万象天成"};
                        break;
                }
                gateArray = new String[]{"请选择"};
                community = null;
                gate = null;
                initCommunity();
                initGate();
                break;
            case R.id.sp_community:
                community = communityArray[position];
                switch (community) {
                    case "豪利花园":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "凯旋新世界广粤尊府":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "翡翠绿洲森林半岛":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "中海康城花园":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "云溪四季":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "国展苑":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "龙珠花园":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "万科清林径":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "英郡年华":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                    case "万象天成":
                        gateArray = new String[]{"请选择", "南门", "1幢", "2幢", "3幢", "4幢", "5幢", "6幢", "7幢", "8幢", "9幢", "10幢", "11幢", "12幢", "13幢", "14幢", "15幢"};
                        break;
                }
                gate = null;
                initGate();
                break;
            case R.id.sp_gate:
                gate = gateArray[position];
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if (!TextUtils.isEmpty(gate)) {
            Logger.i(TAG, "已选择" + community + gate);
            shareUtil.saveBoolean("hasAddress", true);
            shareUtil.saveString("community", community);
            shareUtil.saveString("gate", gate);
            Intent intent = new Intent(this, Main3Activity.class);
            startActivity(intent);
        }
    }

    private void copyBigDataToSD(String strOutFileName) throws IOException {
        Logger.i(TAG, "start copy file " + strOutFileName);
        File sdDir = Environment.getExternalStorageDirectory();//get directory
        File file = new File(sdDir.toString() + "/attendance/");
        if (!file.exists()) {
            file.mkdir();
        }
        String tmpFile = sdDir.toString() + "/attendance/" + strOutFileName;
        File f = new File(tmpFile);
        if (f.exists()) {
            Logger.i(TAG, "file exists " + strOutFileName);
            return;
        }
        InputStream myInput;
        java.io.OutputStream myOutput = new FileOutputStream(sdDir.toString()
                + "/attendance/" + strOutFileName);
        myInput = this.getAssets().open(strOutFileName);
        byte[] buffer = new byte[1024];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }
        myOutput.flush();
        myInput.close();
        myOutput.close();
        Logger.i(TAG, "end copy file " + strOutFileName);
    }
}
