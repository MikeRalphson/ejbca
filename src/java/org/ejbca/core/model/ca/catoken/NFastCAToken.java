/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *************************************************************************/

 package org.ejbca.core.model.ca.catoken;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;

import org.apache.log4j.Logger;


/** This class implements support for the nCipher nFast HSM for storing CA keys.
 * This implementation was done by PrimeKey Solutions AB (www.primekey.se) in 2005 
 * and the development was sponsored by Linagora (www.linagora.com).
 * 
 * @author Lars Silv�n
 * @version $Id: NFastCAToken.java,v 1.9 2006-09-05 13:18:02 primelars Exp $
 */
public class NFastCAToken extends BaseCAToken implements IHardCAToken {

    /** Log4j instance */
    private static final Logger log = Logger.getLogger(NFastCAToken.class);

    static final private String SLOT_LABEL_KEY = "keyStore";
    static final private String PROVIDER_NAME = "nCipherKM";
    static final private String PROVIDER_CLASS = "com.ncipher.provider.km.nCipherKM";

    private KeyStore keyStore; // has to be declared as an atribute because its destructor destoys fetched keys.

    /** The constructor of HardCAToken should throw an InstantiationException if the token can not
     * be created, if for example depending jar files for the particular HSM is not available.
     * @throws InstantiationException
     * @throws IllegalAccessException if the nCipher provider is not available
     */
    public NFastCAToken() throws InstantiationException, IllegalAccessException {
        super(PROVIDER_CLASS, PROVIDER_NAME, SLOT_LABEL_KEY);
        log.debug("Creating NFastCAToken");
    }

    /* (non-Javadoc)
     * @see org.ejbca.core.model.ca.catoken.IHardCAToken#activate(java.lang.String)
     */
    public void activate(String authCode) throws CATokenOfflineException, CATokenAuthenticationFailedException {
        try {
            keyStore = KeyStore.getInstance("nCipher.sworld");
            try {
                keyStore.load(new ByteArrayInputStream(sSlotLabel.getBytes()),
                              null);
            } catch( Exception e) {
                log.debug("Preload maybe not called. Assuming 1/N. Exception was: "+e);
                keyStore.load(new ByteArrayInputStream(sSlotLabel.getBytes()),
                              authCode.toCharArray());
            }
            setKeys(keyStore, authCode);
            log.debug("Keys from "+sSlotLabel+ " activated.");
        } catch( Throwable t ) {
            log.error("Authentication failed for keystore "+sSlotLabel+": "+t );
            CATokenAuthenticationFailedException e = new CATokenAuthenticationFailedException(t.toString());
            e.initCause(t);
            deactivate();
            throw e;
        }
    }
}
