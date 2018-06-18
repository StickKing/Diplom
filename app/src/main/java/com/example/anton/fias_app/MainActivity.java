package com.example.anton.fias_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.os.*;
import android.widget.Toast;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;


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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
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
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {

                Byte value = 1;

                String date = null;




                c = Calendar.getInstance();

                date = DateTuesday(value);

                toast = Toast.makeText(getApplicationContext(),  " " + date, Toast.LENGTH_SHORT);
                toast.show();






                String url_bd = "http://fias.nalog.ru/Public/Downloads/" + date + "/BASE.7Z";


                try {
                    if (isOnline()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                Uri uri = Uri.parse(url_bd);
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "FIAS_BD/" + "FIAS.7Z");
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


    public static Boolean unzip(String sourceFile, String destinationFolder)  {
        ZipInputStream zis = null;

        try {
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(sourceFile)));
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[400000];
            while ((ze = zis.getNextEntry()) != null) {
                String fileName = ze.getName();
                fileName = fileName.substring(fileName.indexOf("/") + 1);
                File file = new File(destinationFolder, fileName);
                File dir = ze.isDirectory() ? file : file.getParentFile();

                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Invalid path: " + dir.getAbsolutePath());
                if (ze.isDirectory()) continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }

            }
        } catch (IOException ioe){
            Log.d(TAG,ioe.getMessage());
            return false;
        }  finally {
            if(zis!=null)
                try {
                    zis.close();
                } catch(IOException e) {

                }
        }
        return true;
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


                    //try {
                        String fl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FIAS_BD/" + "FIAS.7Z";
                        SevenZFile sevenZFile = null;

                        File file2 = new File(path, "FIAS.7Z");


                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1 || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                try {
                                    sevenZFile = new SevenZFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/FIAS_BD/" + "FIAS.7Z"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }


                    //sevenZFile = new SevenZFile(new File(path, "FIAS.7z"));



                        /*SevenZArchiveEntry entry = sevenZFile.getNextEntry();
                        while (entry != null) {

                            FileOutputStream out = new FileOutputStream(entry.getName());
                            byte[] content = new byte[(int) entry.getSize()];
                            sevenZFile.read(content, 0, content.length);
                            out.write(content);
                            out.close();
                            entry = sevenZFile.getNextEntry();
                        }
                        sevenZFile.close();*/

                    //} catch (IOException e) {
                    //    e.printStackTrace();
                    //}
                    toast = Toast.makeText(getApplicationContext(), "Files = " + file.list().length + "   " + file2.exists(), Toast.LENGTH_LONG);
                    toast.show();









                }else{

                    file = new File(path, "FIAS.7Z");



                }



            }















            myWebView.setVisibility(View.GONE);
            myLiner.setVisibility(View.VISIBLE);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
