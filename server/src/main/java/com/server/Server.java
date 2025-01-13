package com.server;

import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.Executors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public class Server {

    private Server() {
    }

    private static SSLContext myServerSSLContext(String file, String password) throws Exception {
        char[] passphrase = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(file), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }

    public static void main(String[] args) throws Exception {
        // create the http server to port 8001 with default logger
        try {
            HttpContext httpContext;
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = myServerSSLContext(args[0], args[1]);
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {

                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });
            MyHandler handler = new MyHandler();

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext));
            // create context that defines path for the resource, in this case a "help"
            UserAuthenticator userAuth = new UserAuthenticator("info");
            httpContext = server.createContext("/info", handler);

            RegistrationHandler regHandler = new RegistrationHandler(userAuth);
            httpContext.setAuthenticator(userAuth);

            server.createContext("/registration", regHandler);
            // creates a default executor
            server.setExecutor(Executors.newCachedThreadPool());
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}