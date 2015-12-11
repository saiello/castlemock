package com.fortmocks.web.basis.model.session.token.repository;

import com.fortmocks.web.basis.model.session.token.SessionToken;
import com.fortmocks.web.basis.model.session.token.SessionTokenList;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * The session token repository is responsible for managing all the ongoing sessions and their corresponding
 * tokens. The repository is also responsible for providing the functionality to both save and load all the
 * tokens to and from the local file system.
 * @author Karl Dahlgren
 * @since 1.0
 * @see SessionToken
 * @see SessionTokenList
 */
@Component("tokenRepository")
public class SessionTokenRepository implements PersistentTokenRepository {

    @Value(value = "${token.file.directory}")
    private String tokenDirectory;
    @Value(value = "${token.file.name}")
    private String tokenFileName;
    private static final Logger LOGGER = Logger.getLogger(SessionTokenRepository.class);
    private final Map<String, PersistentRememberMeToken> seriesTokens = new HashMap();

    /**
     * The initialize method is responsible for initiating the token repository and load
     * all the stored tokens
     */
    public void initiate(){
        loadTokens();
    }

    /**
     * The method provides the functionality to store a new token in the token repository
     * @param token The token that will be stored in the token repository
     */
    public synchronized void createNewToken(PersistentRememberMeToken token) {
        PersistentRememberMeToken current = this.seriesTokens.get(token.getSeries());
        if(current != null) {
            throw new DataIntegrityViolationException("Series Id \'" + token.getSeries() + "\' already exists!");
        } else {
            this.seriesTokens.put(token.getSeries(), token);
            saveTokens();
        }
    }

    /**
     * Updates a specific token with a new values to a specific token
     * @param series The token that will be updated
     * @param tokenValue The new token value
     * @param lastUsed Date for when it was last used
     */
    public synchronized void updateToken(String series, String tokenValue, Date lastUsed) {
        PersistentRememberMeToken token = this.getTokenForSeries(series);
        PersistentRememberMeToken newToken = new PersistentRememberMeToken(token.getUsername(), series, tokenValue, new Date());
        this.seriesTokens.put(series, newToken);
        saveTokens();
    }

    /**
     * The method provides the functionality to update the token with a new username. The token
     * will be identified with the old username and upon found, the username will be updated to the
     * new provided username value
     * @param oldUsername The old username. It is used to identify the token
     * @param newUsername The new username. It will replace the old username
     */
    public synchronized void updateToken(String oldUsername, String newUsername) {
        final List<PersistentRememberMeToken> tokens = new LinkedList<PersistentRememberMeToken>();
        for(PersistentRememberMeToken token : seriesTokens.values()){
            if(token.getUsername().equalsIgnoreCase(oldUsername)){
                PersistentRememberMeToken newToken = new PersistentRememberMeToken(newUsername, token.getSeries(), token.getTokenValue(), token.getDate());
                tokens.add(newToken);
            }
        }
        for(PersistentRememberMeToken token : tokens){
           seriesTokens.put(token.getSeries(), token);
        }
        saveTokens();
    }

    /**
     * Get a specific token for a series
     * @param seriesId The id of the series that the token belongs to
     * @return Token that matches the provided series id. Null will be returned if no token matches
     * the provided series id
     */
    public synchronized PersistentRememberMeToken getTokenForSeries(String seriesId) {
        return this.seriesTokens.get(seriesId);
    }

    /**
     * Remove a user token from the repository
     * @param username The token that matches this user name will be removed
     */
    public synchronized void removeUserTokens(String username) {
        Iterator series = this.seriesTokens.keySet().iterator();

        while(series.hasNext()) {
            String seriesId = (String)series.next();
            PersistentRememberMeToken token = this.seriesTokens.get(seriesId);
            if(username.equals(token.getUsername())) {
                series.remove();
            }
        }
        saveTokens();
    }

    /**
     * Saves all the tokens into the file system
     */
    private synchronized void saveTokens(){
        final SessionTokenList tokens = getTokens();
        final String filename = tokenDirectory + File.separator +  tokenFileName;
        Writer writer = null;
        try {
            JAXBContext context = JAXBContext.newInstance(SessionTokenList.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            writer = new FileWriter(filename);
            marshaller.marshal(tokens, writer);
        } catch (JAXBException e) {
            LOGGER.error("Unable to parse the following file: " + tokenFileName, e);
            throw new IllegalStateException("Unable to parse the following file: " + tokenFileName);
        } catch (IOException e) {
            LOGGER.error("Unable to read file: " + tokenFileName, e);
            throw new IllegalStateException("Unable to read the following file: " + tokenFileName);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.error("Unable to close file writer for type " + tokens.getClass().getSimpleName(), e);
                }
            }
        }
    }

    /**
     * Load all tokens stored on the local file system
     */
    private void loadTokens(){
        final Path path = FileSystems.getDefault().getPath(tokenDirectory);
        if(!Files.exists(path)){
            try {
                LOGGER.debug("Creating the following directory: " + path);
                Files.createDirectories(path);
            } catch (IOException e) {
                LOGGER.error("Unable to create the following directory: " + path, e);
                throw new IllegalStateException("Unable to create the following folder: " + tokenDirectory);
            }
        }
        if(!Files.isDirectory(path)){
            throw new IllegalStateException("The provided path is not a directory: " + path);
        }

        final File file = new File(tokenDirectory + File.separator +  tokenFileName);
        try {
            if (file.isFile()) {
                JAXBContext jaxbContext = JAXBContext.newInstance(SessionTokenList.class);
                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                SessionTokenList tokens = (SessionTokenList) jaxbUnmarshaller.unmarshal(file);
                for(SessionToken token : tokens){
                    PersistentRememberMeToken persistentRememberMeToken = new PersistentRememberMeToken(token.getUsername(), token.getSeries(), token.getTokenValue(), token.getDate());
                    seriesTokens.put(persistentRememberMeToken.getSeries(), persistentRememberMeToken);
                }
                LOGGER.debug("\tLoaded " + file.getName());
            }
        } catch (JAXBException e) {
            LOGGER.error("Unable to parse files for type " + SessionTokenList.class.getSimpleName(), e);
        }
    }

    /**
     * Get all tokens in a token list
     * @return A list of tokens
     */
    private SessionTokenList getTokens(){
        final SessionTokenList tokens = new SessionTokenList();
        for(PersistentRememberMeToken persistentRememberMeToken : seriesTokens.values()){
            final SessionToken token = new SessionToken(persistentRememberMeToken);
            tokens.add(token);
        }
        return tokens;
    }


}
