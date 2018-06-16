package com.example.anton.fias_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import java.util.Calendar;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
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
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

import android.os.*;
import android.widget.Toast;




public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {



    private WebView myWebView; //Объявляю webview
    private LinearLayout myLiner;

    private DownloadManager downloadManager;
    private Context context = null;
    private static final String TAG = null;
    private int REQUEST_CODE;

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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_CALENDAR}, REQUEST_CODE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CALENDAR}, REQUEST_CODE);

        //Присоединяю переменную webview к webview на моём активити
        myWebView = (WebView) findViewById(R.id.Web);
        myLiner = (LinearLayout) findViewById(R.id.offline);

        //Делаю так чтобы ссылки не открывались во внешних браузерах
        myWebView.setWebViewClient(new MyWebViewClient());

        //Масштабирование, так для большей наглядности
        myWebView.getSettings().setSupportZoom(true);
        myWebView.getSettings().setBuiltInZoomControls(true);

        //Включаю использование JavaScript
        myWebView.getSettings().setJavaScriptEnabled(true);

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
            @TargetApi(Build.VERSION_CODES.N)
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {

                Byte value = 1;

                //if ()

                String date = DateTuesday(value);
                toast = Toast.makeText(getApplicationContext(), date, Toast.LENGTH_SHORT);
                toast.show();






                String url_bd = "http://fias.nalog.ru/Public/Downloads/20180607/BASE.7Z";


                /*try{
                    file_download (url_bd);
                }
                catch(Exception e){
                    //Обработайте ошибку
                    toast.show();
                }*/
                try {
                    if (isOnline()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                Uri uri = Uri.parse(url_bd);
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/FIAS_BD.7Z");
                                Long reference = downloadManager.enqueue(request);

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



        c.add(Calendar.DATE, -minus);

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        if (dayOfWeek != 5) {
            minus++;
            return DateTuesday(minus);
        } else{
            if ((c.get(c.MONTH) + 1) >= 10 ) {

                if ( (c.get(c.DAY_OF_MONTH)) >= 10 ){return (c.get(c.YEAR) + "." + (c.get(c.MONTH) + 1) + "." + c.get(c.DAY_OF_MONTH) );}
                else {return (c.get(c.YEAR) + "." + (c.get(c.MONTH) + 1) + ".0" + c.get(c.DAY_OF_MONTH) );}

            }
            else {

                if ( (c.get(c.DAY_OF_MONTH)) >= 10 ){return (c.get(c.YEAR) + ".0" + (c.get(c.MONTH) + 1) + "." + c.get(c.DAY_OF_MONTH) );}
                else {return (c.get(c.YEAR) + ".0" + (c.get(c.MONTH) + 1) + ".0" + c.get(c.DAY_OF_MONTH) );}

            }


        }

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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.general) {
            if (isOnline()) {

                myWebView.loadUrl("http://fias.nalog.ru/");

            }
            else{

                myWebView.loadData("" +
                        "<html>" +
                        "   <body>" +
                        "       <h1>Возникла проблема с сетью</h1>" +
                        "       <p>Причины по которым это могло произойти:</p>" +
                        "       <hr>" +

                        "       <ul>"+
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
            myLiner.setVisibility(View.GONE);

        } else if (id == R.id.offline) {

            myWebView.setVisibility(View.GONE);
            myLiner.setVisibility(View.VISIBLE);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
