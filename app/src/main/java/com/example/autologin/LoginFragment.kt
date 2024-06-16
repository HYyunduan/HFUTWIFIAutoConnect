package com.example.autologin

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.example.autologin.databinding.ActivityMainBinding
import com.example.autologin.databinding.FragmentLoginBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.wait
import java.io.IOException
import java.lang.StringBuilder
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import java.util.Enumeration
import java.net.NetworkInterface

class LoginFragment :Fragment() {
    private var loginStatus:Boolean = false
    private var _binding:FragmentLoginBinding?=null
    private val binding get() = _binding!!
    private lateinit var handler : Handler
    private lateinit var sp:SharedPreferences
    private var debugMode:Boolean=false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sp = requireContext().getSharedPreferences("sp_config",Context.MODE_PRIVATE)
        _binding = FragmentLoginBinding.inflate(inflater,container,false)
        //setContentView(binding.root)
        debugMode=sp.getBoolean("debugMode",false)
        binding.checkBox.isChecked = sp.getBoolean("savedPassword",false)
        if(binding.checkBox.isChecked){
            binding.editTextText.setText(sp.getString("account",""))
            binding.editTextTextPassword.setText(sp.getString("password",""))
        }
        binding.button.setOnClickListener{
            clickButton()//requireContext())
        }
        binding.setting.setOnClickListener{
            clickSetting()
        }
        handler =object :Handler(Looper.getMainLooper()){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val alertDialog = AlertDialog.Builder(requireContext())
                    .setMessage(msg.obj?.toString())
                    .setIcon(R.mipmap.ic_launcher)
                    .setPositiveButton("确定", DialogInterface.OnClickListener { dialog, which ->
                    })
                alertDialog.show()
            }
        }
        return binding.root
    }
    private fun clickButton(){//context:Context){
        var acName:String?=null
        var userIp:String?=null
        var macAddr:String?=null
        var acIp:String?=null
        //binding.textView2.text="Log:"
        fun runThis(case:Int){//,IP:String?,i:String) {
            lateinit var url: String;
            if (acName.equals(null)) {
                acName = ""
            }
            if (acIp.equals(null)) {
                acIp = ""
            }
            if (macAddr.equals(null)) {
                macAddr = "000000000000"
            }
            when (case) {
                1 -> url =
                    "http://210.45.240.105:801/eportal/?c=Portal&a=login&callback=dr1003&login_method=1&user_account=${binding.editTextText.text}&user_password=${binding.editTextTextPassword.text}&wlan_user_ip=${userIp}&wlan_user_ipv6=&wlan_user_mac=${macAddr}&wlan_ac_ip=${acIp}&wlan_ac_name=${acName}&jsVersion=3.3.2&v=3754"

                2 -> url =
                    "http://210.45.240.105:801/eportal/?c=Portal&a=login&callback=dr1003&login_method=8&user_account=${binding.editTextText.text}&user_password=${binding.editTextTextPassword.text}&wlan_user_ip=${userIp}&wlan_user_ipv6=&wlan_user_mac=${macAddr}&wlan_ac_ip=${acIp}&wlan_ac_name=${acName}&jsVersion=3.3.2&v=5906"
            }
            //val url = "http://www.icourse163.org/"
            println(url)
            val request: Request = Request.Builder()
                .get()
                .url(url).build()
            val client: OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .build()

            /*client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Looper.prepare();
                    val message =
                        Message.obtain(handler, 1, "failed to connect to server")//e.toString())
                    handler.sendMessage(message)
                    Looper.loop()
                }

                override fun onResponse(call: Call, response: Response) {
                    Looper.prepare();
                    val string = response.body?.string()
                    if (string != null) {
                        if("\'result\':\'1\'" in string){
                            val message = Message.obtain(handler, 1, "登录成功")
                            loginStatus=true
                            handler.sendMessage(message)
                            return
                        }
                    }
                    Looper.loop()
                }
            })*/
            try {
                val response = client.newCall(request).execute()
                val string = response.body?.string()
                if (string != null) {
                    if ("\"result\":\"1\"" in string) {
                        val message = Message.obtain(handler, 1, "登录成功")
                        loginStatus = true
                        handler.sendMessage(message)
                    }
                }
                println(string)
            } catch (e: Exception) {
                print(e)
            }
        }
        loginStatus=false
        fun testInternet(){
            //搞定这吐人的跳转
            var url = "http://www.icourse163.org/"
            var request: Request = Request.Builder()
                .get()
                .url(url).build()
            var client: OkHttpClient = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(3, TimeUnit.SECONDS)
                .followRedirects(false)
                .build()
            try {
                val response = client.newCall(request).execute()
                var string:String?=null
                string = response.header("Location")
                if(string == null) {
                    //第一次接受的返回值可能是刷新、跳转命令
                    url = "http://www.icourse163.org/?cmd=redirect&arubalp=12345"
                    request = Request.Builder()
                        .get()
                        .url(url).build()
                    client = OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .readTimeout(3, TimeUnit.SECONDS)
                        .followRedirects(false)
                        .build()
                    val response = client.newCall(request).execute()
                    val body_string = response.body?.string()
                    val message = Message.obtain(
                        handler,
                        1,
                        url + response.headers.toString() + body_string
                    )
                    if(debugMode){
                        handler.sendMessage(message)
                    }
                    string = response.header("Location")
                    if (string == null) {
                        string =body_string
                    }
                }
                if (string != null && "210.45.240.105" in string) {
                    val patternUserIp = Regex("((wlanuserip)|(&ip))=(\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3})")
                    val patternAcName = Regex("wlanacname=(.{1,16})(&|\")")
                    val patternMacAddr = Regex("mac=((\\w{2}:\\w{2}:\\w{2}:\\w{2}:\\w{2}:\\w{2})|(\\w{2}-\\w{2}-\\w{2}-\\w{2}-\\w{2}-\\w{2}))")
                    val patternAcIp = Regex("switchip=(\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3})")
                    acName = patternAcName.find(string)?.groupValues?.get(1)
                    userIp = patternUserIp.find(string)?.groupValues?.get(4)
                    macAddr = patternMacAddr.find(string)?.groupValues?.get(1)?.replace(":","")?.replace("-","")
                    acIp = patternAcIp.find(string)?.groupValues?.get(1)
                    if(debugMode){
                        val m =string+"\nuserip:"+userIp+"\nacname:"+acName+"\nmacaddress"+macAddr
                        val message = Message.obtain(handler, 1, m)
                        handler.sendMessage(message)
                    }

                    //binding.textView2.append(string+'\n')
                    //binding.textView2.append(userIp+'\n'+acIp+'\n'+acName+'\n'+macAddr+'\n')
                }else{
                    var message = Message.obtain(handler, 1, "已经成功连接到互联网了")
                    val responseString = response.body?.string()
                    handler.sendMessage(message)
                    if(debugMode){
                        message = Message.obtain(handler, 1,"response:"+responseString)
                        handler.sendMessage(message)
                    }
                    //binding.textView2.append(response.headers.toString()+'\n')
                    //binding.textView2.append(responseString+'\n')
                    loginStatus = true
                }
                println(string)
            } catch (e: Exception) {
                println(e)
                var message = Message.obtain(handler, 1, "可能未连接wifi，请检查网络设置")
                handler.sendMessage(message)
                loginStatus=true
                if(debugMode){
                    message = Message.obtain(handler, 1, e.toString())
                    handler.sendMessage(message)
                }
            }
        }

        val firstThread = Thread{
            run{
                testInternet()
            }
        }
        firstThread.start()
        firstThread.join()

        /*val wlan_ac_name:List<String> = listOf("HFUT-WS7880","","Ruijie_Ac_61f6c6","H3CWX5540X")
        val wlan_ac_ip:List<String> = listOf("222.195.2.4","222.195.2.12")
        val IP = getLocalHostExactAddress();*/
        /*for (i in wlan_ac_name){
             Thread {
                //var localHost:InetAddress  = InetAddress.getLocalHost();
                //222.195.2.4
                run {
                    //runThis(1,IP,i)
                    runThis(1)
                }
             }.start()
        }*/
        //for(i in wlan_ac_ip) {
            val threadLast = Thread {
                run {
                    //runThis(2, IP, i)
                    if(!loginStatus){
                        runThis(1)
                    }
                    if(!loginStatus) {
                        runThis(2)
                    }
                }
            }
            threadLast.start()
            //if(i.equals(wlan_ac_ip[wlan_ac_ip.lastIndex])){
                threadLast.join()
            //}
       // }

        if(!loginStatus){
            val message =
                Message.obtain(handler, 1, "登陆失败")//e.toString())
            handler.sendMessage(message)
        }
        if(binding.checkBox.isChecked){
            sp.edit().putString("account",binding.editTextText.text.toString())?.apply()
            sp.edit().putString("password",binding.editTextTextPassword.text.toString())?.apply()
            sp.edit().putBoolean("savedPassword",true).apply()
        }else{
            sp.edit().putString("account","").apply()
            sp.edit().putString("password","").apply()
            sp.edit().putBoolean("savedPassword",false).apply()
        }
    }

    private fun clickSetting(){
        val settingFragment = SettingFragment()
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_activity_main, settingFragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }
    private fun getLocalHostExactAddress(): String? {
        try {
            val networkInterfaces:Enumeration<NetworkInterface>  = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                var iface:NetworkInterface  = networkInterfaces.nextElement();
                println(iface.hardwareAddress)
                // 该网卡接口下的ip会有多个，也需要一个个的遍历，找到自己所需要的
                val enumInetAddress = iface.inetAddresses
                while(enumInetAddress.hasMoreElements()) {
                    val inetAddress = enumInetAddress.nextElement()
                    // 排除loopback回环类型地址（不管是IPv4还是IPv6 只要是回环地址都会返回true）
                    if (inetAddress.isSiteLocalAddress) {
                        if(inetAddress.hostAddress.length<=15){
                            println(inetAddress.hostAddress)
                            return inetAddress.hostAddress
                        }
                    }
                        // 若不是site-local地址 那就记录下该地址当作候选
                }
            }

            // 如果出去loopback回环地之外无其它地址了，那就回退到原始方案吧
        } catch (e:Exception) {
            println(e)
        }
        return null;
    }
}

