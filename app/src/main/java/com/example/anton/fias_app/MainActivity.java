package com.example.anton.fias_app;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.os.*;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import ru.smartflex.tools.dbf.DbfEngine;
import ru.smartflex.tools.dbf.DbfHeader;
import ru.smartflex.tools.dbf.DbfIterator;
import ru.smartflex.tools.dbf.DbfRecord;
import android.widget.AdapterView.OnItemSelectedListener;



public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {


    //Компоненты интерфейса
    private WebView myWebView; //Объявляю webview
    private LinearLayout offline;
    private Button s_but;
    private ProgressBar p_bar;
    Spinner spinner;
    LinearLayout linearLayout;
    EditText s_text;
    TextView s_view;
    Button b;

    //Потоки
    GorodaThread gh = null;
    DownloadThread dh = null;
    SearchCodeGorod scg = null;
    //SearchStreetCode sc = null;

    String code_g = null;
    String c_s = "всё плохо";

    private DownloadManager downloadManager;
    private Context context = null;
    private static final String TAG = null;
    private int REQUEST_CODE;
    Runnable runnable = null;
    Thread thread = null;
    ArrayList<String> n_g = new ArrayList<String>();
    ArrayList<String> c_g = new ArrayList<String>();
    ArrayList<String> code_s = new ArrayList<String>();
    ArrayList<String> name_s = new ArrayList<String>();


    ArrayAdapter<String> adapter;


    Calendar c = Calendar.getInstance();

    Toast toast = null;



    private class MyWebViewClient extends android.webkit.WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);
            return true;
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Спрашиваем у пользователя разрешение на использование его пространства на устройстве
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, REQUEST_CODE);

        b = (Button) findViewById(R.id.b);
        s_view = (TextView) findViewById(R.id.textView);
        s_but = (Button)findViewById(R.id.search);
        p_bar = (ProgressBar)findViewById(R.id.progressBar);
        spinner = (Spinner)findViewById(R.id.spinner);
        linearLayout = (LinearLayout) findViewById(R.id.progree_lay);
        ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.id_c);
        s_text = (EditText) findViewById(R.id.editText);
        //Присоединяю переменную webview к webview на моём активити
        myWebView = (WebView) findViewById(R.id.Web);
        offline = (LinearLayout) findViewById(R.id.offline);

        //Делаю так чтобы ссылки не открывались во внешних браузерах
        myWebView.setWebViewClient(new MyWebViewClient());

        //Масштабирование, так для большей наглядности
        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setBuiltInZoomControls(true);

        //Включаю использование JavaScript
        myWebView.getSettings().setJavaScriptEnabled(true);


        linearLayout.setVisibility(View.INVISIBLE);
        myWebView.setVisibility(View.VISIBLE);

        if (isOnline()) {
            myWebView.loadUrl("http://fias.nalog.ru/");
        }else{
            myWebView.loadData("" +
                    "<html>" +
                    "   <body>" +
                    "       <h1>Возникла проблема с сетью</h1>" +
                            "<p>Причины по которым это могло произойти:</p>" +
                    "       <hr>" +

                           " <ul>"+
			                    "<li>Отсутствует подключение к интернету</li>" +
			                    "<li>Сайт на данный момент не доступен</li>" +
			                    "<li>Вы подключены к специфичной сети</li>" +
		                    "</ul>" +
                    "   </body>" +
                    "" +
                    "</html>", "text/html", "UTF-8");
            toast = Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_SHORT);
            toast.show();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                try {
                    if (isOnline()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                                dh = new DownloadThread();
                                dh.start();

                                Snackbar.make(view, "Загрузка базы данных началась", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();

                            } else {
                                Snackbar.make(view, "Отсутствует разрешение на загрузку", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        }
                    } else {
                        Snackbar.make(view, "Отсутствует подключение к интернету", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }
                catch(Exception e){
                    Snackbar.make(view, "Загрузить базу данных на данный момент не возможно", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }


            }

        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }



    //Проверяю подключение к интернету
    protected boolean isOnline() {
        String cs = Context.CONNECTIVITY_SERVICE;
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(cs);
        if (cm.getActiveNetworkInfo() == null) {
            return false;
        } else return true;
    }

    //Рекурсия возвращающая дату предыдущего четверга
    protected  String DateTuesday(Byte minus){

        c = Calendar.getInstance();

        c.add(Calendar.DATE, -minus);

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek != 5) {
            minus++;
            return DateTuesday(minus);
        } else{
            if ((c.get(c.MONTH) + 1) >= 10 ) {

                if ( (c.get(c.DAY_OF_MONTH)) >= 10 ){return (c.get(c.YEAR) + "" + (c.get(c.MONTH) + 1) + "" + c.get(c.DAY_OF_MONTH) );}
                else {return (c.get(c.YEAR) + "" + (c.get(c.MONTH) + 1) + "0" + c.get(c.DAY_OF_MONTH) );}

            }
            else {

                if ( (c.get(c.DAY_OF_MONTH)) >= 10 ){return (c.get(c.YEAR) + "0" + (c.get(c.MONTH) + 1) + "" + c.get(c.DAY_OF_MONTH) );}
                else {return (c.get(c.YEAR) + "0" + (c.get(c.MONTH) + 1) + "0" + c.get(c.DAY_OF_MONTH) );}

            }


        }

    }



    public void un7zip() throws IOException {

        SevenZFile sevenZFile = null;
        try {

            sevenZFile = new SevenZFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FIAS_BD/" + "FIAS.7Z"));
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            OutputStream os = new FileOutputStream(entry.getName());
            while (entry != null) {
                byte[] buffer = new byte[8192];//
                int count;
                while ((count = sevenZFile.read(buffer, 0, buffer.length)) > -1) {

                    os.write(buffer, 0, count);
                }
                entry = sevenZFile.getNextEntry();
            }
            sevenZFile.close();
            os.close();

            /*while (entry != null) {

                FileOutputStream out = new FileOutputStream(entry.getName());
                byte[] content = new byte[(int) entry.getSize()];
                sevenZFile.read(content, 0, content.length);
                out.write(content);
                out.close();
                entry = sevenZFile.getNextEntry();
            }
            sevenZFile.close();*/

        } catch (IOException e) {


        }

    }



    class GorodaThread extends Thread {
        public void run() {
            try {
                System.out.println("Gorod thread begin");
                final ArrayList<Integer> id_g = new ArrayList<Integer>();
                Message msg = handler.obtainMessage();
                String str = null;

                n_g.clear();
                c_g.clear();

                DbfHeader dbfHeader = DbfEngine.getHeader(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FIAS_BD/" + "KLADR.dbf"), null);
                DbfIterator dbfIterator = dbfHeader.getDbfIterator();
                while (dbfIterator.hasMoreRecords()) {
                    DbfRecord dbfRecord = dbfIterator.nextRecord();
                    str = dbfRecord.getString("SOCR");
                    if ((str.equals("г") && (dbfRecord.getString("STATUS").equals("3") || dbfRecord.getString("STATUS").equals("2"))) || (str.equals("г") && (dbfRecord.getString("NAME").equals("Москва") || dbfRecord.getString("NAME").equals("Санкт-Петербург")))) {
                        n_g.add(dbfRecord.getString("NAME"));
                        c_g.add(dbfRecord.getString("CODE"));
                    }
                }


                handler.sendMessage(msg);
                System.out.println("Gorod thread finished");
            } catch (Exception e) {

            }
        }
    }

    @SuppressLint("HandlerLeak") Handler handler = new Handler(){
        public void handleMessage(Message msg) {

            linearLayout.setVisibility(View.INVISIBLE);
            offline.setVisibility(View.VISIBLE);
            spinner.setAdapter(adapter);

        }
    };


    class DownloadThread extends Thread {
        public void run() {
            try {

                System.out.println("Main thread begin");

                Byte value = 1;
                String date = null;
                c = Calendar.getInstance();
                date = DateTuesday(value);
                String url_bd = "http://fias.nalog.ru/Public/Downloads/" + date + "/BASE.7Z";
                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(url_bd);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FIAS_BD/" + "FIAS.7Z");
                Long reference = downloadManager.enqueue(request);

                System.out.println("Main thread finished");
            } catch (Exception e) {

            }
        }
    }

    @SuppressLint("HandlerLeak") Handler handlertwo = new Handler(){
        public void handleMessage(Message msg) {

            s_view.setText("Ищем улицу");

        }
    };



    @SuppressLint("HandlerLeak") Handler handlerthree = new Handler(){
        public void handleMessage(Message msg) {

            offline.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
            s_view.setText(c_s);

        }
    };

    class SearchCodeGorod extends Thread {
        public void run() {
            try {

                System.out.println("gorod thread begin");
                ArrayList<String> codes = new ArrayList<String>();

                for (Integer i = 0; i < n_g.size(); i++){

                    if (spinner.getSelectedItem().toString().equals(n_g.get(i))){
                        code_g = c_g.get(i);
                    }

                }



                DbfHeader dbfHeader = DbfEngine.getHeader(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FIAS_BD/" + "STREET.dbf"), null);
                DbfIterator dbfIterator = dbfHeader.getDbfIterator();
                while (dbfIterator.hasMoreRecords()) {
                    DbfRecord dbfRecord = dbfIterator.nextRecord();

                            if (s_text.getText().toString().equals(dbfRecord.getString("NAME"))){

                                codes.add(dbfRecord.getString("CODE"));

                            }

                }









                System.out.println("gorod thread finished " + code_g + " " + c_s);
            } catch (Exception e) {

            }
        }
    }



    public void S_Click(View view) {

        if (gh.isAlive() == false){

            String search_text = s_text.getText().toString();

            offline.setVisibility(View.INVISIBLE);
            linearLayout.setVisibility(View.VISIBLE);
            s_view.setText("Определяем город");

            scg = new SearchCodeGorod();
            scg.setPriority(10);
            scg.start();


            //toast = Toast.makeText(getApplicationContext(), "Давай ссука " + s_text.getText() + " " + sg.isAlive(), Toast.LENGTH_SHORT);
           // toast.show();

        }



    }

    public void BClick(View view) {


        toast = Toast.makeText(getApplicationContext(), "код улицы " + c_s + " " + s_text.getText().toString(), Toast.LENGTH_SHORT);
        toast.show();


    }



    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if(myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }

        /*if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }*/

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.general) {

            if (gh.isAlive() == false) {
                if (isOnline()) {

                    myWebView.loadUrl("http://fias.nalog.ru/");

                } else {

                    myWebView.loadData("" +
                            "<html>" +
                            "   <body>" +
                            "       <h1>Возникла проблема с сетью</h1>" +
                            "       <p>Причины по которым это могло произойти:</p>" +
                            "       <hr>" +

                            "       <ul>" +
                            "           <li>Отсутствует подключение к интернету</li>" +
                            "           <li>Сайт на данный момент не доступен</li>" +
                            "           <li>Вы подключены к специфичной сети</li>" +
                            "       </ul>" +
                            " </body>" +
                            "" +
                            "</html>", "text/html", "UTF-8");

                    toast = Toast.makeText(getApplicationContext(), "Отсутствует подключение к интернету", Toast.LENGTH_SHORT);
                    toast.show();

                }
                myWebView.setVisibility(View.VISIBLE);
                offline.setVisibility(View.GONE);
            }else {


                toast = Toast.makeText(getApplicationContext(), "Дождитесь окончания загрузки автономного режима", Toast.LENGTH_SHORT);
                toast.show();

            }

        } else if (id == R.id.offline) {



            if ((dh.isAlive() == false)){

                myWebView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.VISIBLE);
                offline.setVisibility(View.INVISIBLE);
                // проверяем был ли уже загружен файл
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/FIAS_BD");
                File file = new File(path, "FIAS.7z");
                //Проверяю существует ли архив с базой данных
                if (!file.exists()){
                    toast = Toast.makeText(getApplicationContext(), "База данных отсутствует, загрузите её нажав на кнопку в правом нижнем углу", Toast.LENGTH_LONG);
                    toast.show();
                }else{

                    //Проверяю существует ли в папке с базой данных разорхивированые файлы
                    file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FIAS_BD");

                    if (file.isDirectory() && (file.list().length < 9)){

                        String fl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FIAS_BD/" + "FIAS.7Z";
                        SevenZFile sevenZFile = null;
                        File file2 = new File(path, "FIAS.7Z");
                        toast = Toast.makeText(getApplicationContext(), "Files = " + file.list().length + "   " + file2.exists(), Toast.LENGTH_LONG);
                        toast.show();
                    }else{

                        p_bar.setVisibility(View.VISIBLE);
                        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, n_g);

                        spinner.OnItemSelectedListener itemSelectedListener = new spinner.OnItemSelectedListener() {

                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                // Получаем выбранный объект
                                String item = (String) parent.getItemAtPosition(position);

                                if (spinner.getSelectedItem().toString().equals("Москва")){
                                    toast = Toast.makeText(getApplicationContext(), "lf cerf", Toast.LENGTH_LONG);
                                    toast.show();
                                }


                            }
                        };


                        gh = new GorodaThread();
                        gh.setPriority(10);
                        gh.start();








                    }
                }


            } else {

                toast = Toast.makeText(getApplicationContext(), "Загрузка не завершена, дождитесь окончания загрузки и повторите попытку", Toast.LENGTH_LONG);
                toast.show();

            }



        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



}
