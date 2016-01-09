package trash;

import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class JavaYoutubeDownloader {

    public static String newline = System.getProperty("line.separator");
    private static final Logger log = Logger.getLogger(JavaYoutubeDownloader.class.getCanonicalName());
    private static final Level defaultLogLevelSelf = Level.FINER;
    private static final Level defaultLogLevel = Level.WARNING;
    private static final String scheme = "http";
    private static final String host = "www.youtube.com";
    private static final Pattern commaPattern = Pattern.compile(",");
    private static final Pattern pipePattern = Pattern.compile("\\|");
    private static final char[] ILLEGAL_FILENAME_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':'};


    private static List<Pair<String, String>> parseDecoded(String qs, String split) throws UnsupportedEncodingException {
        String[] arr = qs.split("&");
        List<Pair<String, String>> ret = new ArrayList<>();
        Log.d("===","parsing " + qs);
        for (String str : arr) {

            if (split.equals(",")) {
                ret.add(new Pair<String, String>("", str));
            } else {
                String[] akv = str.split("=");
                if (akv.length==2) {
                    String key = java.net.URLDecoder.decode(akv[0], "UTF-8");
                    String val = java.net.URLDecoder.decode(akv[1], "UTF-8");
                    ret.add(new Pair<String, String>(key, val));
                }
            }
        }
        return ret;
    }


    public static File doDownload(String videoId, int format, String encoding, String userAgent, File outputDir, String extension) throws Throwable {

        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("video_id", videoId));
        qparams.add(new BasicNameValuePair("fmt", "" + format));
        URI uri = getUri("get_video_info", qparams);
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(uri);
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();

        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
        HttpResponse response = httpclient.execute(httpget, localContext);
        HttpEntity entity = response.getEntity();

        httpget.setHeader("User-Agent", userAgent);

        if (entity == null || response.getStatusLine().getStatusCode() != 200) {
            Log.d("=!=", "No response");
            return null;
        }

        //read the info stream
        InputStream inStream = entity.getContent();
        String videoInfo = getStringFromInputStream(encoding, inStream);
        if (videoInfo == null || videoInfo.length() <= 0) {
            Log.d("=!=", "No info");
            return null;
        }
        Log.d("=!=", videoInfo);
        List<Pair<String,String>> infoMap = new ArrayList<>();
        //URLEncodedUtils.parse(infoMap, new Scanner(videoInfo), encoding);
        infoMap = parseDecoded(videoInfo,"&");
        for (Pair<String,String> pair : infoMap) {
            String key = pair.first;
            String val = pair.second;
            Log.d("key", key);

            if (key.equals("url_encoded_fmt_stream_map")) {
                String sig = "";

                if (key.equals("signature")){
                    sig=val;
                }
                /*String decoded = java.net.URLDecoder.decode(val, "UTF-8");
                Log.d("=!=", "Decoded " + decoded);

                String[] formats = commaPattern.split(decoded);
                String firstFormat = "";
                for (String f : formats) {
                    if (f.contains("url=")) {
                        firstFormat = f;
                        break;
                    }
                }
                Log.d("=!=", "First format " + firstFormat);
                url = firstFormat.substring(firstFormat.indexOf("url=") + 4);
                if (url.contains(";")) {
                    url = url.substring(0, url.lastIndexOf(";"));
                } else {
                    url = url.substring(0, url.length());
                }
                }else{
                */

                List<Pair<String,String>> second = parseDecoded(val,",");
                for (Pair<String,String> s : second){
                    List<Pair<String,String>> third = parseDecoded(s.second,"&");
                    String fallback_host = "";

                    String url = "";
                    for (Pair<String,String> thirdIt : third){
                        if (thirdIt.first.equals("url")){
                            url = thirdIt.second;
                        }
                        if (thirdIt.first.equals("url")){
                            fallback_host = thirdIt.second;
                        }
                        if (thirdIt.first.equals("signature")){
                            Log.d("THIS",s.second);
                            if (!thirdIt.second.isEmpty()) {
                                sig = thirdIt.second;
                            }
                        }
                    }

                    if (!url.contains("&fallback_host=") && !fallback_host.isEmpty()) {
                        url += ("&fallback_host=" + URLEncoder.encode(fallback_host, "UTF-8"));
                    }
                    if (!url.contains("&signature=") && !sig.isEmpty()) {
                        url += ("&signature=" + URLEncoder.encode(sig, "UTF-8"));
                    }

                    Log.d("OUTURL",url);
                }
            }
        }


            /*
            foreach (var kv in list) {
                if (kv[0] != "url_encoded_fmt_stream_map") continue;
                var list2 = ParseFormDecoded(kv[1], ',');
                foreach (var kv2 in list2) {
                    var list3 = ParseFormDecoded(kv2[1]);
                    string url = "";
                    string fallback_host = "";
                    string sig = "";
                    foreach (var kv3 in list3) {
                        switch (kv3[0]) {
                            case "url":
                                url = kv3[1];
                                break;
                            case "fallback_host":
                                fallback_host = kv3[1];
                                break;
                            case "sig":
                                sig = kv3[1];
                                break;
                        }
                    }
                    if (url.IndexOf("&fallback_host=", StringComparison.Ordinal) < 0)
                        url += "&fallback_host=" + HttpUtility.UrlEncode(fallback_host);
                    if (url.IndexOf("&signature=", StringComparison.Ordinal) < 0)
                        url += "&signature=" + HttpUtility.UrlEncode(sig);
                    urls.Add(new Uri(url));
                }
            }
            return urls;
            */


return null;
        /*
        if (url == null || url.isEmpty()) {
            return null;
        }


        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File file = new File(outputDir, videoId);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
        downloadWithHttpClient(userAgent, url, file);
        return file;
        */
    }

    private static void downloadWithHttpClient(String userAgent, String downloadUrl, File outputfile) throws Throwable {
        Log.d("=!=", "Downloading content");
        Log.d("=!=", downloadUrl);
        HttpGet httpget2 = new HttpGet(downloadUrl);
        httpget2.setHeader("User-Agent", userAgent);

        log.finer("Executing " + httpget2.getURI());
        HttpClient httpclient2 = new DefaultHttpClient();
        HttpResponse response2 = httpclient2.execute(httpget2);
        HttpEntity entity2 = response2.getEntity();
        Log.d("=!=", response2.getStatusLine().getStatusCode() + "");
        if (entity2 != null && response2.getStatusLine().getStatusCode() == 200) {
            long length = entity2.getContentLength();
            InputStream instream2 = entity2.getContent();
            log.finer("Writing " + length + " bytes to " + outputfile);
            if (outputfile.exists()) {
                outputfile.delete();
            }
            FileOutputStream outstream = new FileOutputStream(outputfile);
            try {
                byte[] buffer = new byte[2048];
                int count = -1;
                Log.d("=!=", "Starting wihile");
                while ((count = instream2.read(buffer)) != -1) {
                    //Log.d("=!=", "Reading " + count);
                    outstream.write(buffer, 0, count);
                }
                outstream.flush();
            } finally {
                outstream.close();
            }
        } else {

        }
    }

    public static File playAlternate(String videoId, int format, String encoding, String userAgent, File outputdir, String extension) throws Throwable {
        File outputfile = null;
        if (!outputdir.exists()) {
            outputdir.mkdirs();
        }
        List<NameValuePair> qparams = new ArrayList<NameValuePair>();
        qparams.add(new BasicNameValuePair("video_id", videoId));
        qparams.add(new BasicNameValuePair("fmt", "" + format));
        URI uri = getUri("get_video_info", qparams);

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(uri);
        httpget.setHeader("User-Agent", userAgent);

        HttpResponse response = httpclient.execute(httpget, localContext);
        HttpEntity entity = response.getEntity();
        if (entity != null && response.getStatusLine().getStatusCode() == 200) {
            InputStream instream = entity.getContent();
            String videoInfo = getStringFromInputStream(encoding, instream);
            if (videoInfo != null && videoInfo.length() > 0) {
                Log.d("=!=", "Video info" + videoInfo);

                List<NameValuePair> infoMap = new ArrayList<NameValuePair>();
                URLEncodedUtils.parse(infoMap, new Scanner(videoInfo), encoding);
                String token = null;
                String downloadUrl = null;
                String filename = videoId;

                for (NameValuePair pair : infoMap) {
                    String key = pair.getName();
                    String val = pair.getValue();

                    if (key.equals("token")) {
                        token = val;
                    } else if (key.equals("title")) {
                        filename = val;
                    } else if (key.equals("url_encoded_fmt_stream_map")) {
                        String decoded = java.net.URLDecoder.decode(val, "UTF-8");
                        Log.d("=!=", "Decoded " + decoded);

                        String[] formats = commaPattern.split(decoded);
                        String firstFormat = "";
                        for (String f : formats) {
                            if (f.contains("url=")) {
                                firstFormat = f;
                                break;
                            }
                        }


                        //first available format
                        Log.d("f0", firstFormat);
                        String url = firstFormat.substring(firstFormat.indexOf("url=") + 4);
                        url = url.substring(0, url.lastIndexOf(";"));
                        downloadUrl = url;

                        //for (String fmt : formats) {
                        //String[] fmtPieces = pipePattern.split(fmt);
                        //for (int i=0;i<fmtPieces.length;i++) {
                        //    Log.d("pieces",fmtPieces[i]);
                        //}
                            /*for (int i=0;i<fmtPieces.)
                            if (fmtPieces.length == 2) {
                                // in the end, download somethin!
                                downloadUrl = fmtPieces[1];
                                Log.d("download",downloadUrl);
                                int pieceFormat = Integer.parseInt(fmtPieces[0]);
                                if (pieceFormat == format) {
                                    // found what we want
                                    downloadUrl = fmtPieces[1];
                                    break;
                                }
                            }
                        }*/
                    }
                }

                filename = cleanFilename(filename);
                if (filename.length() == 0) {
                    filename = videoId;
                } else {
                    filename += "_" + videoId;
                }
                filename += "." + extension;
                outputfile = new File(outputdir, filename);
                Log.d("=!=", "Dowload url" + downloadUrl);
                if (downloadUrl != null) {
                    downloadWithHttpClient(userAgent, downloadUrl, outputfile);
                }
            }
        }
        return outputfile;
    }


    private static String cleanFilename(String filename) {
        for (char c : ILLEGAL_FILENAME_CHARACTERS) {
            filename = filename.replace(c, '_');
        }
        return filename;
    }

    private static URI getUri(String path, List<NameValuePair> qparams) throws URISyntaxException {
        URI uri = URIUtils.createURI(scheme, host, -1, "/" + path, URLEncodedUtils.format(qparams, "UTF-8"), null);
        return uri;
    }

    private static String getStringFromInputStream(String encoding, InputStream instream) throws UnsupportedEncodingException, IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(instream, encoding));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            instream.close();
        }
        String result = writer.toString();
        return result;
    }
}