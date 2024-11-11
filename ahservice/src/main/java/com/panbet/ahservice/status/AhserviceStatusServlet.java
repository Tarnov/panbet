package com.panbet.ahservice.status;


import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.HttpRequestHandler;

import com.panbet.otherutils.ManifestReader;


public class AhserviceStatusServlet implements HttpRequestHandler
{
    private ManifestReader manifestReader;

    @Override
    public void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter writer = response.getWriter();
        response.setContentType("text/html; charset=UTF-8");
        writer.print(
                "<!DOCTYPE html>\n<html>\n<head><title>Ahservice status</title></head><body><b>Mhservice status</b><br><pre>");
        manifestReader.writeProperties(writer);
        writer.print("</pre></body></html>");

    }

    @Required
    public void setManifestReader(ManifestReader manifestReader)
    {
        this.manifestReader = manifestReader;
    }

}
