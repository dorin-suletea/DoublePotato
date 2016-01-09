package com.dsu.dev4fun.doublepotato.model.downloader;

import android.os.Environment;
import android.util.Log;

import com.dsu.dev4fun.doublepotato.model.util.AppException;
import com.dsu.dev4fun.doublepotato.model.util.DownloadManualInterruptedException;
import com.dsu.dev4fun.doublepotato.model.util.MethodWrapper;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebYoutubeDownloader {
    private final String SRC_TAG = "source";

    private String executePost(String targetURL, String urlParameters) {
        HttpURLConnection connection = null;
        try {
            //Create connection
            URL url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");


            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                ;
                response.append('\r');
            }
            rd.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public File fetchVideo(String url, String name, String vidID, MethodWrapper<Float> postProgress, AtomicBoolean isRunning) throws DownloadManualInterruptedException {
        File outputFile = null;
        try {
            String pageSrc = executePost(url, "");
            Log.d("=!=", "Downloading song " + name + "from url " + url);
            String srcUrl = getSourceURL(pageSrc);

            String fileName = name + "-" + vidID + ".mp4";
            //replace all empty spaces with _ in order to be accepted by ffmpeg
            fileName = fileName.replaceAll(" ","_");

            outputFile = downloadStream(srcUrl, fileName, postProgress, isRunning);
        } catch (AppException e1) {
            Log.d("=!=", e1.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile;
    }


    private String getSourceURL(String pageSrc) throws AppException {
        //extract url links
        List<String> parsedLinks = extractUrls(pageSrc);
        List<String> sourceUrls = new ArrayList<>();
        for (String s : parsedLinks) {
            if (s.contains(SRC_TAG)) {
                sourceUrls.add(s);
            }
        }

        if (sourceUrls.isEmpty()) {
            throw new AppException("Keepvid bug,skip");
        }

        return sourceUrls.get(0);
    }

    private List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    private File downloadStream(String url, String newFileName, MethodWrapper<Float> postProgress, AtomicBoolean isRunning) throws IOException, DownloadManualInterruptedException {
        URL u = new URL(url);
        URLConnection uc = u.openConnection();
        int contentLength = uc.getContentLength();
        InputStream raw = uc.getInputStream();
        InputStream in = new BufferedInputStream(raw);
        byte[] data = new byte[1024];
        int bytesRead = 0;
        int offset = 0;
        //file
        File outputDir = Environment.getExternalStorageDirectory();
        File outputFile = new File(outputDir, newFileName);
        if (outputFile.exists()) {
            outputFile.delete();
        }
        outputFile.createNewFile();
        FileOutputStream out = new FileOutputStream(outputFile);

        while (offset < contentLength) {
            if (!isRunning.get()) {
                Log.d("=!=", "Download interrupted");
                Float progress = 0.0f;
                postProgress.execute(progress);
                out.close();
                in.close();
                throw new DownloadManualInterruptedException();
            }

            bytesRead = in.read(data);
            if (bytesRead == -1)
                break;
            out.write(data, 0, bytesRead);
            offset = offset + bytesRead;
            Float progress = (((float) offset) / ((float) contentLength));
            postProgress.execute(progress);
        }
        in.close();

        if (offset != contentLength) {
            throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
        }
        out.flush();
        out.close();
        return outputFile;
    }
}
