package com.mkyong.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;
import java.net.URL;

@Path("/file")
public class UploadFileService {

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {

        // save it
        String result = writeToFile(uploadedInputStream);

        return Response.status(200).entity(result).build();

    }

    // save uploaded file to new location
    private String writeToFile(InputStream uploadedInputStream) {

        try {

        	System.out.println("Loading models ... ");
            Configuration configuration = new Configuration();

            // Load model from the jar
            configuration.setAcousticModelPath("resource:/models/acoustic/wsj_8kHz");

            // You can also load model from folder
            // configuration.setAcousticModelPath("file:en-us");

            configuration.setDictionaryPath("resource:/models/acoustic/wsj_8kHz/dict/cmudict.0.6d");

            configuration.setLanguageModelPath("resource:/models/language/en-us.lm.dmp");
            System.out.println("Loaded models !");

            StreamSpeechRecognizer recognizer =
                    new StreamSpeechRecognizer(configuration);
            System.out.println("Streaming speech recognizer ...");
            recognizer.startRecognition(uploadedInputStream);
            
            System.out.println("Start recording ...");
            SpeechResult result = recognizer.getResult();
            String response = null;
            
            if(result != null) {
            	response = result.getHypothesis();
            }

//            while ((result = recognizer.getResult()) != null) {
//
//                System.out.format("Hypothesis: %s\n",
//                        result.getHypothesis());
//                response = result.getHypothesis();
//
//                System.out.println("List of recognized words and their times:");
//                for (WordResult r : result.getWords()) {
//                    System.out.println(r);
//                }
//
//                System.out.println("Best 3 hypothesis:");
//                for (String s : result.getNbest(3)) {
//                    System.out.println(s);
//                }
//
//                System.out.println("Lattice contains " + result.getLattice().getNodes().size() + " nodes");
//            }

            recognizer.stopRecognition();
            return response;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return null;

    }
}