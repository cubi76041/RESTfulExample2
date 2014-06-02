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
            configuration.setAcousticModelPath("resource:/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz");
            
            // You can also load model from folder
            // configuration.setAcousticModelPath("file:en-us");
            
            configuration.setDictionaryPath("resource:/WSJ_8gau_13dCep_16k_40mel_130Hz_6800Hz/dict/cmudict.0.6d");

            configuration.setLanguageModelPath("resource:/models/language/en-us.lm.dmp");
            System.out.println("Loaded models !");

            StreamSpeechRecognizer recognizer =
                    new StreamSpeechRecognizer(configuration);
            System.out.println("Streaming speech recognizer ...");
            recognizer.startRecognition(uploadedInputStream);
            
            System.out.println("Start recording ...");
            SpeechResult result;
            String response = "";
            int i = 1;

            while ((result = recognizer.getResult()) != null) {

                System.out.format("Hypothesis: %s\n",
                        result.getHypothesis());
                
                response += response == "" ? "" : "\n\n";
                response += "Statement " + String.valueOf(i) + ": \n";
                response += "\tResult: " + result.getHypothesis() + "\n";

                System.out.println("List of recognized words and their times:");
                for (WordResult r : result.getWords()) {
                    System.out.println(r);
                }

                System.out.println("Best 3 hypothesis:");
                response += "\t Best 3 results: \n";
                for (String s : result.getNbest(3)) {
                    System.out.println(s);
                    response += "\t\t" + s.replace("<s>", "").replace("</s>", "").trim() + "\n";
                }

                System.out.println("Lattice contains " + result.getLattice().getNodes().size() + " nodes");
                i++;
            }
            
            response = "<pre>" + response + "</pre>";

            recognizer.stopRecognition();
            return response;
        } catch (IOException e) {

            e.printStackTrace();
        }
        return null;

    }
}