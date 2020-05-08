package com.company;

import org.apache.commons.codec.binary.Base64;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

@SuppressWarnings("Duplicates")
public class RestApiClient {

    public static void main(String[] args) throws IOException, JSONException, ParseException, URISyntaxException {

        Scanner scan = new Scanner(System.in);
        boolean adgangNægtet = true;
        boolean doneWithProgram = false;
        String bruger = null;
        String kodeord = null;
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);

        while (adgangNægtet) {
            System.out.println("Bruger:");
            bruger = scan.next();
            System.out.println("Adgangskode:");
            kodeord = scan.next();

            try {
                if (login(bruger, kodeord)) {
                    adgangNægtet = false;
                    System.out.println("Bruger godkendt");
                }
            } catch (Exception e) {
                //e.printStackTrace();
                System.out.println("Bruger ikke godkendt" + "Prøv igen.");
                System.out.println("-------------------------");
            }
        }
        while (!doneWithProgram) {
            int brugerValg = mainMenu();

            switch (brugerValg) {
                case 1:
                    boolean valg1 = false;
                    while (!valg1) {
                        System.out.println("               ");
                        System.out.println("Følgende bookings er optaget idag:");
                        System.out.println(getBookingsOfDate(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), bruger, kodeord));
                        System.out.println("Ønsker du at booke et lokale idag? Så indtast følgende oplysninger:");
                        System.out.println("-------------------------");
                        System.out.println("1 for 08:00-12:00");
                        System.out.println("2 for 12:00-16:00");
                        System.out.println("3 for 16:00-20:00");
                        System.out.println("4 for 20:00-24:00");
                        int timeblock = scan.nextInt();
                        System.out.println("-------------------------");
                        System.out.println("Hvilket rum? id fra 1-6");
                        int roomId = scan.nextInt();
                        System.out.println("-------------------------");
                        System.out.println("Laver din booking...");
                        createBooking(timeblock, roomId, getUserid(bruger, kodeord), bruger, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), bruger, kodeord);
                        System.out.println("Din booking er blevet lavet.");
                        valg1 = true;
                    }
                    break;
                case 2:
                    System.out.println("Finder dine bookings...");
                    System.out.println("--------------------------------------------------------------------------------------------------");
                    System.out.println(getBookings(getUserid(bruger, kodeord), bruger, kodeord));

                    break;
                case 3:
                    System.out.println("Finder dine oplysninger...");
                    System.out.println("-------------------------");
                    System.out.println("Userid : " + getUserid(bruger, kodeord));
                    System.out.println("Brugernavn : " + bruger);
                    break;
                case 4:
                    System.out.println("Logger ud...");
                    doneWithProgram = true;
                    break;
                default:

            }
        }
    }

    public static int mainMenu() {
        int valg;
        Scanner sc = new Scanner(System.in);
        System.out.println("-------------------------");
        System.out.println("Menu");
        System.out.println("1 - Ny bookning");
        System.out.println("2 - Find dine bookninger");
        System.out.println("3 - Brugeroplysninger");
        System.out.println("4 - Log ud");
        System.out.println("-------------------------");
        valg = sc.nextInt();
        return valg;
    }

    public static boolean login(String username, String password) throws IOException, JSONException {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpPost post = new HttpPost("http://ec2-3-21-232-61.us-east-2.compute.amazonaws.com:8081/login");

            JSONObject payload = new JSONObject();
            payload.put("username", username);
            payload.put("password", password);
            post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
            // En response handler taget fra Apaches side: https://hc.apache.org/httpcomponents-client-5.0.x/httpclient5/examples/ClientWithResponseHandler.java
            final HttpClientResponseHandler<String> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                    final HttpEntity entity = response.getEntity();
                    try {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } catch (final ParseException ex) {
                        throw new ClientProtocolException(ex);
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            final String responseBody = httpclient.execute(post, responseHandler);
            System.out.println("-------------------------");
            if (responseBody.equals("true")) {
                return true;
            } else {
                System.out.println("false");
                return false;
            }
        }
    }

    public static String getBookingsOfDate(int day, int month, int year, String username, String password) throws IOException, JSONException, ParseException, URISyntaxException {

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet get = new HttpGet("http://ec2-3-21-232-61.us-east-2.compute.amazonaws.com:8081/bookings/findByDate/" + day + "/" + month + "/" + year);

            String header = "Basic ";
            String headerValue = username + ":" + password;
            String encodedHeaderValue = Base64.encodeBase64String(headerValue.getBytes());
            String headerBasic = header + encodedHeaderValue;
            get.addHeader(HttpHeaders.AUTHORIZATION, headerBasic);

            // En response handler taget fra Apaches side: https://hc.apache.org/httpcomponents-client-5.0.x/httpclient5/examples/ClientWithResponseHandler.java
            final HttpClientResponseHandler<String> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                    final HttpEntity entity = response.getEntity();
                    try {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } catch (final ParseException ex) {
                        throw new ClientProtocolException(ex);
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }

            };
            final String responseBody = httpclient.execute(get, responseHandler);
            String responseNew = responseBody.replace("[", "");
            String responseNew1 = responseNew.replace("{", "|");
            String responseNew2 = responseNew1.replace("}", "|" + "\n");
            String responseNew3 = responseNew2.replace("\"", "");
            String respnsNew4 = responseNew3.replace(",", " ");
            String respnsNew5 = respnsNew4.replace("]", "");
            String respnsNew6 = respnsNew5.replace(" ", " - ");
            return respnsNew6;
        }
    }

    public static void createBooking(int timeblock, int roomId, String userid, String username, int month, int year, int day, String userName, String password) throws IOException, JSONException {
        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpPost post = new HttpPost("http://ec2-3-21-232-61.us-east-2.compute.amazonaws.com:8081/bookings");

            JSONObject payload = new JSONObject();
            payload.put("timeblock", timeblock);
            payload.put("roomId", roomId);
            payload.put("userid", userid);
            payload.put("username", username);
            payload.put("month", month);
            payload.put("year", year);
            payload.put("day", day);
            post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));

            String header = "Basic ";
            String headerValue = username + ":" + password;
            String encodedHeaderValue = Base64.encodeBase64String(headerValue.getBytes());
            String headerBasic = header + encodedHeaderValue;
            post.addHeader(HttpHeaders.AUTHORIZATION, headerBasic);

            // En response handler taget fra Apaches side: https://hc.apache.org/httpcomponents-client-5.0.x/httpclient5/examples/ClientWithResponseHandler.java
            final HttpClientResponseHandler<String> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                    final HttpEntity entity = response.getEntity();
                    try {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } catch (final ParseException ex) {
                        throw new ClientProtocolException(ex);
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status + " Couldnt create booking");
                }
            };
            final String responseBody = httpclient.execute(post, responseHandler);
            System.out.println("-------------------------");
        }
    }

    public static String getUserid(String username, String password) throws IOException, JSONException, ParseException, URISyntaxException {

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet get = new HttpGet("http://ec2-3-21-232-61.us-east-2.compute.amazonaws.com:8081/users/username/" + username);

            String header = "Basic ";
            String headerValue = username + ":" + password;
            String encodedHeaderValue = Base64.encodeBase64String(headerValue.getBytes());
            String headerBasic = header + encodedHeaderValue;
            get.addHeader(HttpHeaders.AUTHORIZATION, headerBasic);

            // En response handler taget fra Apaches side: https://hc.apache.org/httpcomponents-client-5.0.x/httpclient5/examples/ClientWithResponseHandler.java
            final HttpClientResponseHandler<String> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                    final HttpEntity entity = response.getEntity();
                    try {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } catch (final ParseException ex) {
                        throw new ClientProtocolException(ex);
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }

            };
            final String responseBody = httpclient.execute(get, responseHandler);
            CloseableHttpResponse result = httpclient.execute(get);
            HttpEntity entity = result.getEntity();
            String content = EntityUtils.toString(entity);
            JSONObject myObject = new JSONObject(content);
            return myObject.get("id").toString();
        }
    }

    public static String getBookings(String userid, String username, String password) throws IOException, JSONException, ParseException {

        try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
            final HttpGet get = new HttpGet("http://ec2-3-21-232-61.us-east-2.compute.amazonaws.com:8081/bookings/user/" + userid);
            String header = "Basic ";
            String headerValue = username + ":" + password;
            String encodedHeaderValue = Base64.encodeBase64String(headerValue.getBytes());
            String headerBasic = header + encodedHeaderValue;
            get.addHeader(HttpHeaders.AUTHORIZATION, headerBasic);

            // En response handler taget fra Apaches side: https://hc.apache.org/httpcomponents-client-5.0.x/httpclient5/examples/ClientWithResponseHandler.java
            final HttpClientResponseHandler<String> responseHandler = response -> {
                final int status = response.getCode();
                if (status >= HttpStatus.SC_SUCCESS && status < HttpStatus.SC_REDIRECTION) {
                    final HttpEntity entity = response.getEntity();
                    try {
                        return entity != null ? EntityUtils.toString(entity) : null;
                    } catch (final ParseException ex) {
                        throw new ClientProtocolException(ex);
                    }
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }

            };
            final String responseBody = httpclient.execute(get, responseHandler);
            String responseNew = responseBody.replace("[", "");
            String responseNew1 = responseNew.replace("{", "|");
            String responseNew2 = responseNew1.replace("}", "|" + "\n");
            String responseNew3 = responseNew2.replace("\"", "");
            String respnsNew4 = responseNew3.replace(",", " ");
            String respnsNew5 = respnsNew4.replace("]", "");
            String respnsNew6 = respnsNew5.replace(" ", " - ");
            return respnsNew6;
        }
    }
}

