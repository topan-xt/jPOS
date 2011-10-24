/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2011 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package  org.jpos.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.javatuples.Pair;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;
import org.jpos.util.*;
import org.jpos.util.NameRegistrar.NotFoundException;


/**
 * <p>
 * Provides base functionality for the actual Security Module Adapter.
 * </p>
 * <p>
 * You adapter needs to override the methods that end with "Impl"
 * </p>
 * @author Hani S. Kirollos
 * @version $Revision$ $Date$
 */
public class BaseSMAdapter
        implements SMAdapter, Configurable, LogSource {
    protected Logger logger = null;
    protected String realm = null;
    protected Configuration cfg;
    private String name;

    public BaseSMAdapter () {
        super();
    }

    public BaseSMAdapter (Configuration cfg, Logger logger, String realm) throws ConfigurationException
    {
        super();
        setLogger(logger, realm);
        setConfiguration(cfg);
    }

    public void setConfiguration (Configuration cfg) throws ConfigurationException {
        this.cfg = cfg;
    }

    public void setLogger (Logger logger, String realm) {
        this.logger = logger;
        this.realm = realm;
    }

    public Logger getLogger () {
        return  logger;
    }

    public String getRealm () {
        return  realm;
    }

    /**
     * associates this SMAdapter with a name using NameRegistrar
     * @param name name to register
     * @see NameRegistrar
     */
    public void setName (String name) {
        this.name = name;
        NameRegistrar.register("s-m-adapter." + name, this);
    }

    /**
     * @return this SMAdapter's name ("" if no name was set)
     */
    public String getName () {
        return  this.name;
    }

    /**
     * @param name
     * @return SMAdapter instance with given name.
     * @throws NotFoundException
     * @see NameRegistrar
     */
    public static SMAdapter getSMAdapter (String name) throws NameRegistrar.NotFoundException {
        return  (SMAdapter)NameRegistrar.get("s-m-adapter." + name);
    }

    public SecureDESKey generateKey (short keyLength, String keyType) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key Length", keyLength), new SimpleMsg("parameter",
                    "Key Type", keyType)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate Key", cmdParameters));
        SecureDESKey result = null;
        try {
            result = generateKeyImpl(keyLength, keyType);
            evt.addMessage(new SimpleMsg("result", "Generated Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }


    public byte[] generateKeyCheckValue (SecureDESKey kd) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key with untrusted check value", kd)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate Key Check Value", cmdParameters));
        byte[] result = null;
        try {
            result = generateKeyCheckValueImpl(kd);
            evt.addMessage(new SimpleMsg("result", "Generated Key Check Value", ISOUtil.hexString(result)));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public SecureDESKey importKey (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key Length", keyLength), new SimpleMsg("parameter",
                    "Key Type", keyType), new SimpleMsg("parameter", "Encrypted Key",
                    encryptedKey), new SimpleMsg("parameter", "Key-Encrypting Key", kek), new SimpleMsg("parameter", "Check Parity", checkParity)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Import Key", cmdParameters));
        SecureDESKey result = null;
        try {
            result = importKeyImpl(keyLength, keyType, encryptedKey, kek, checkParity);
            evt.addMessage(new SimpleMsg("result", "Imported Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public byte[] exportKey (SecureDESKey key, SecureDESKey kek) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key", key), new SimpleMsg("parameter", "Key-Encrypting Key",
                    kek),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Export Key", cmdParameters));
        byte[] result = null;
        try {
            result = exportKeyImpl(key, kek);
            evt.addMessage(new SimpleMsg("result", "Exported Key", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN encryptPIN (String pin, String accountNumber, boolean extract) throws SMException {
        accountNumber = extract ? EncryptedPIN.extractAccountNumberPart(accountNumber) : accountNumber;
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "clear pin", pin), new SimpleMsg("parameter", "account number",
                    accountNumber)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Encrypt Clear PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = encryptPINImpl(pin, accountNumber);
            evt.addMessage(new SimpleMsg("result", "PIN under LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }
    public EncryptedPIN encryptPIN (String pin, String accountNumber) throws SMException {
        return encryptPIN(pin, accountNumber, true);
    }

    public String decryptPIN (EncryptedPIN pinUnderLmk) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Decrypt PIN", cmdParameters));
        String result = null;
        try {
            result = decryptPINImpl(pinUnderLmk);
            evt.addMessage(new SimpleMsg("result", "clear PIN", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN importPIN (EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1), new SimpleMsg("parameter",
                    "Data Key 1", kd1),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Import PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = importPINImpl(pinUnderKd1, kd1);
            evt.addMessage(new SimpleMsg("result", "PIN under LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN translatePIN (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1), new SimpleMsg("parameter",
                    "Data Key 1", kd1), new SimpleMsg("parameter", "Data Key 2", kd2),
                    new SimpleMsg("parameter", "Destination PIN Block Format", destinationPINBlockFormat)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate PIN from Data Key 1 to Data Key 2",
                cmdParameters));
        EncryptedPIN result = null;
        try {
            result = translatePINImpl(pinUnderKd1, kd1, kd2, destinationPINBlockFormat);
            evt.addMessage(new SimpleMsg("result", "PIN under Data Key 2", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN importPIN (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under Derived Unique Key", pinUnderDuk), new SimpleMsg("parameter",
                    "Key Serial Number", ksn), new SimpleMsg("parameter", "Base Derivation Key",
                    bdk)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Import PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = importPINImpl(pinUnderDuk, ksn, bdk);
            evt.addMessage(new SimpleMsg("result", "PIN under LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN translatePIN (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under Derived Unique Key", pinUnderDuk), new SimpleMsg("parameter",
                    "Key Serial Number", ksn), new SimpleMsg("parameter", "Base Derivation Key",
                    bdk), new SimpleMsg("parameter", "Data Key 2", kd2), new SimpleMsg("parameter",
                    "Destination PIN Block Format", destinationPINBlockFormat)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = translatePINImpl(pinUnderDuk, ksn, bdk, kd2, destinationPINBlockFormat);
            evt.addMessage(new SimpleMsg("result", "PIN under Data Key 2", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN exportPIN (EncryptedPIN pinUnderLmk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk), new SimpleMsg("parameter",
                    "Data Key 2", kd2), new SimpleMsg("parameter", "Destination PIN Block Format",
                    destinationPINBlockFormat)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Export PIN", cmdParameters));
        EncryptedPIN result = null;
        try {
            result = exportPINImpl(pinUnderLmk, kd2, destinationPINBlockFormat);
            evt.addMessage(new SimpleMsg("result", "PIN under Data Key 2", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public EncryptedPIN generatePIN(String accountNumber, int pinLen)
            throws SMException {
      return generatePIN(accountNumber, pinLen, null);
    }

    public EncryptedPIN generatePIN(String accountNumber, int pinLen, List<String> excludes)
            throws SMException {
      List<SimpleMsg> cmdParameters = new ArrayList<SimpleMsg>();
      cmdParameters.add(new SimpleMsg("parameter", "account number", accountNumber));
      cmdParameters.add(new SimpleMsg("parameter", "PIN length", pinLen));
      if(excludes != null && !excludes.isEmpty())
        cmdParameters.add(new SimpleMsg("parameter", "Excluded PINS list", excludes));

      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Generate PIN", cmdParameters.toArray(new SimpleMsg[0])));
      EncryptedPIN result = null;
      try {
        result = generatePINImpl(accountNumber, pinLen, excludes);
        evt.addMessage(new SimpleMsg("result", "Generate PIN", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    public String calculatePVV(EncryptedPIN pinUnderLMK, SecureDESKey pvkA,
                               SecureDESKey pvkB, int pvkIdx) throws SMException {
      SimpleMsg[] cmdParameters = {
        new SimpleMsg("parameter", "account number", pinUnderLMK.getAccountNumber()),
        new SimpleMsg("parameter", "PIN under LMK", pinUnderLMK),
        new SimpleMsg("parameter", "PVK-A", pvkA == null ? "" : pvkA),
        new SimpleMsg("parameter", "PVK-B", pvkB == null ? "" : pvkB),
        new SimpleMsg("parameter", "PVK index", pvkIdx)};
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PVV", cmdParameters));
      String result = null;
      try {
        result = calculatePVVImpl(pinUnderLMK, pvkA, pvkB, pvkIdx);
        evt.addMessage(new SimpleMsg("result", "Calculate PVV", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    public boolean verifyPVV(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey pvkA,
                          SecureDESKey pvkB, int pvki, String pvv) throws SMException {

      SimpleMsg[] cmdParameters = {
        new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()),
        new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1),
        new SimpleMsg("parameter", "Data Key 1", kd1),
        new SimpleMsg("parameter", "PVK-A", pvkA == null ? "" : pvkA),
        new SimpleMsg("parameter", "PVK-B", pvkB == null ? "" : pvkB),
        new SimpleMsg("parameter", "pvki", pvki),
        new SimpleMsg("parameter", "pvv", pvv)
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify a PIN Using the VISA Method", cmdParameters));

      try {
        boolean r = verifyPVVImpl(pinUnderKd1, kd1, pvkA, pvkB, pvki, pvv);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public String calculateIBMPINOffset(EncryptedPIN pinUnderLmk, SecureDESKey pvk,
                                        String decTab, String pinValData,
                                        int minPinLen) throws SMException {
      SimpleMsg[] cmdParameters = {
        new SimpleMsg("parameter", "account number", pinUnderLmk.getAccountNumber()),
        new SimpleMsg("parameter", "PIN under LMK", pinUnderLmk),
        new SimpleMsg("parameter", "PVK", pvk),
        new SimpleMsg("parameter", "decimalisation table", decTab),
        new SimpleMsg("parameter", "PIN validation data", pinValData),
        new SimpleMsg("parameter", "minimum PIN length", minPinLen)
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate PIN offset", cmdParameters));
      String result = null;
      try {
        result = calculateIBMPINOffsetImpl(pinUnderLmk, pvk,
                decTab, pinValData, minPinLen);
        evt.addMessage(new SimpleMsg("result", "Calculate PIN offset", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    public boolean verifyIBMPINOffset(EncryptedPIN pinUnderKd1, SecureDESKey kd1, SecureDESKey pvk,
                                      String offset, String decTab, String pinValData,
                                      int minPinLen) throws SMException {
      SimpleMsg[] cmdParameters = {
        new SimpleMsg("parameter", "account number", pinUnderKd1.getAccountNumber()),
        new SimpleMsg("parameter", "PIN under Data Key 1", pinUnderKd1),
        new SimpleMsg("parameter", "Data Key 1", kd1),
        new SimpleMsg("parameter", "PVK", pvk),
        new SimpleMsg("parameter", "Pin block format", pinUnderKd1.getPINBlockFormat()),
        new SimpleMsg("parameter", "decimalisation table", decTab),
        new SimpleMsg("parameter", "PIN validation data", pinValData),
        new SimpleMsg("parameter", "minimum PIN length", minPinLen),
        new SimpleMsg("parameter", "offset", offset)
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify PIN offset", cmdParameters));

      try {
        boolean r = verifyIBMPINOffsetImpl(pinUnderKd1, kd1, pvk, offset, decTab,
                 pinValData, minPinLen);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public EncryptedPIN deriveIBMPIN(String accountNo, SecureDESKey pvk,
                                     String decTab, String pinValData,
                                     int minPinLen, String offset) throws SMException {
      SimpleMsg[] cmdParameters = {
        new SimpleMsg("parameter", "account number", accountNo),
        new SimpleMsg("parameter", "Offset", offset),
        new SimpleMsg("parameter", "PVK", pvk), 
        new SimpleMsg("parameter", "Decimalisation table", decTab),
        new SimpleMsg("parameter", "PIN validation data", pinValData),
        new SimpleMsg("parameter", "Minimum PIN length", minPinLen)
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Derive a PIN Using the IBM Method", cmdParameters));
      EncryptedPIN result = null;
      try {
        result = deriveIBMPINImpl(accountNo, pvk, decTab,  pinValData, minPinLen,  offset);
        evt.addMessage(new SimpleMsg("result", "Derive a PIN Using the IBM Method", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    public String calculateCVV(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                               Date expDate, String serviceCode) throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "account number", accountNo),
            new SimpleMsg("parameter", "cvk-a", cvkA == null ? "" : cvkA),
            new SimpleMsg("parameter", "cvk-b", cvkB == null ? "" : cvkB),
            new SimpleMsg("parameter", "Exp date", expDate),
            new SimpleMsg("parameter", "Service code", serviceCode)
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Calculate CVV/CVC", cmdParameters));
      String result = null;
      try {
        result = calculateCVVImpl(accountNo, cvkA, cvkB, expDate, serviceCode);
        evt.addMessage(new SimpleMsg("result", "Calculate CVV/CVC", result));
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
      return result;
    }

    public boolean verifyCVV(String accountNo , SecureDESKey cvkA, SecureDESKey cvkB,
                            String cvv, Date expDate, String serviceCode) throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "account number", accountNo),
            new SimpleMsg("parameter", "cvk-a", cvkA == null ? "" : cvkA),
            new SimpleMsg("parameter", "cvk-b", cvkB == null ? "" : cvkB),
            new SimpleMsg("parameter", "CVV/CVC", cvv),
            new SimpleMsg("parameter", "Exp date", expDate),
            new SimpleMsg("parameter", "Service code", serviceCode)
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify CVV/CVC", cmdParameters));
      try {
        boolean r = verifyCVVImpl(accountNo, cvkA, cvkB, cvv, expDate, serviceCode);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public boolean verifyARQC(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, byte[] transData) throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "skd method", skdm),
            new SimpleMsg("parameter", "imk-ac", imkac),
            new SimpleMsg("parameter", "account number", accoutNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)),
            new SimpleMsg("parameter", "trans. data", transData == null ? "" : ISOUtil.hexString(transData))
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify ARQC/TC/AAC", cmdParameters));
      try {
        boolean r = verifyARQCImpl( mkdm, skdm, imkac, accoutNo, acctSeqNo, arqc, atc, upn, transData);
        evt.addMessage(new SimpleMsg("result", "Verification status", r ? "valid" : "invalid"));
        return r;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public byte[] generateARPC(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "skd method", skdm),
            new SimpleMsg("parameter", "imk-ac", imkac),
            new SimpleMsg("parameter", "account number", accoutNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)),
            new SimpleMsg("parameter", "arpc gen. method", arpcMethod),
            new SimpleMsg("parameter", "auth. rc", arc == null ? "" : ISOUtil.hexString(arc)),
            new SimpleMsg("parameter", "prop auth. data", propAuthData == null
                                       ? "" : ISOUtil.hexString(propAuthData))
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Genarate ARPC", cmdParameters));
      try {
        byte[] result = generateARPCImpl( mkdm, skdm, imkac, accoutNo, acctSeqNo
                           ,arqc, atc, upn, arpcMethod, arc, propAuthData );
        evt.addMessage(new SimpleMsg("result", "ARPC", result));
        return result;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public Pair<Boolean,byte[]> verifyARQCGenerateARPC(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accoutNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,byte[] transData, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {

      SimpleMsg[] cmdParameters = {
            new SimpleMsg("parameter", "mkd method", mkdm),
            new SimpleMsg("parameter", "skd method", skdm),
            new SimpleMsg("parameter", "imk-ac", imkac),
            new SimpleMsg("parameter", "account number", accoutNo),
            new SimpleMsg("parameter", "accnt seq no", acctSeqNo),
            new SimpleMsg("parameter", "arqc", arqc == null ? "" : ISOUtil.hexString(arqc)),
            new SimpleMsg("parameter", "atc", atc == null ? "" : ISOUtil.hexString(atc)),
            new SimpleMsg("parameter", "upn", upn == null ? "" : ISOUtil.hexString(upn)),
            new SimpleMsg("parameter", "trans. data", transData == null ? "" : ISOUtil.hexString(transData)),
            new SimpleMsg("parameter", "arpc gen. method", arpcMethod),
            new SimpleMsg("parameter", "auth. rc", arc == null ? "" : ISOUtil.hexString(arc)),
            new SimpleMsg("parameter", "prop auth. data", propAuthData == null
                                       ? "" : ISOUtil.hexString(propAuthData))
      };
      LogEvent evt = new LogEvent(this, "s-m-operation");
      evt.addMessage(new SimpleMsg("command", "Verify AC and Genarate ARPC", cmdParameters));
      try {
        Pair<Boolean,byte[]> result = verifyARQCGenerateARPCImpl( mkdm, skdm, imkac, accoutNo,
                acctSeqNo, arqc, atc, upn, transData, arpcMethod, arc, propAuthData );
        SimpleMsg[] cmdResult = {
            new SimpleMsg("result1", "arqc verified", result.getValue0()),
            new SimpleMsg("result2", "arpc", result.getValue1() == null
                                             ? "" : ISOUtil.hexString(result.getValue1()))
        };
        evt.addMessage(new SimpleMsg("result", "Verification status and ARPC", cmdResult));
        return result;
      } catch (Exception e) {
        evt.addMessage(e);
        throw e instanceof SMException ? (SMException) e : new SMException(e);
      } finally {
        Logger.log(evt);
      }
    }

    public byte[] generateCBC_MAC (byte[] data, SecureDESKey kd) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "data", data), new SimpleMsg("parameter", "data key",
                    kd),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate CBC-MAC", cmdParameters));
        byte[] result = null;
        try {
            result = generateCBC_MACImpl(data, kd);
            evt.addMessage(new SimpleMsg("result", "CBC-MAC", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public byte[] generateEDE_MAC (byte[] data, SecureDESKey kd) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "data", data), new SimpleMsg("parameter", "data key",
                    kd),
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Generate EDE-MAC", cmdParameters));
        byte[] result = null;
        try {
            result = generateEDE_MACImpl(data, kd);
            evt.addMessage(new SimpleMsg("result", "EDE-MAC", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public SecureDESKey translateKeyFromOldLMK (SecureDESKey kd) throws SMException {
        SimpleMsg[] cmdParameters =  {
            new SimpleMsg("parameter", "Key under old LMK", kd)
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Translate Key from old to new LMK", cmdParameters));
        SecureDESKey result = null;
        try {
            result = translateKeyFromOldLMKImpl(kd);
            evt.addMessage(new SimpleMsg("result", "Translated Key under new LMK", result));
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
        return  result;
    }

    public void eraseOldLMK () throws SMException {
        SimpleMsg[] cmdParameters =  {
        };
        LogEvent evt = new LogEvent(this, "s-m-operation");
        evt.addMessage(new SimpleMsg("command", "Erase the key change storage", cmdParameters));
        try {
            eraseOldLMKImpl();
        } catch (Exception e) {
            evt.addMessage(e);
            throw  e instanceof SMException ? (SMException) e : new SMException(e);
        } finally {
            Logger.log(evt);
        }
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param keyLength
     * @param keyType
     * @return generated key
     * @throws SMException
     */
    protected SecureDESKey generateKeyImpl (short keyLength, String keyType) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param kd
     * @return generated Key Check Value
     * @throws SMException
     */
    protected byte[] generateKeyCheckValueImpl (SecureDESKey kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param keyLength
     * @param keyType
     * @param encryptedKey
     * @param kek
     * @return imported key
     * @throws SMException
     */
    protected SecureDESKey importKeyImpl (short keyLength, String keyType, byte[] encryptedKey,
            SecureDESKey kek, boolean checkParity) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param key
     * @param kek
     * @return exported key
     * @throws SMException
     */
    protected byte[] exportKeyImpl (SecureDESKey key, SecureDESKey kek) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pin
     * @param accountNumber
     * @return encrypted PIN under LMK
     * @throws SMException
     */
    protected EncryptedPIN encryptPINImpl (String pin, String accountNumber) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderLmk
     * @return clear pin as entered by card holder
     * @throws SMException
     */
    protected String decryptPINImpl (EncryptedPIN pinUnderLmk) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd1
     * @param kd1
     * @return imported pin
     * @throws SMException
     */
    protected EncryptedPIN importPINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd1
     * @param kd1
     * @param kd2
     * @param destinationPINBlockFormat
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl (EncryptedPIN pinUnderKd1, SecureDESKey kd1,
            SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @return imported pin
     * @throws SMException
     */
    protected EncryptedPIN importPINImpl (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderDuk
     * @param ksn
     * @param bdk
     * @param kd2
     * @param destinationPINBlockFormat
     * @return translated pin
     * @throws SMException
     */
    protected EncryptedPIN translatePINImpl (EncryptedPIN pinUnderDuk, KeySerialNumber ksn,
            SecureDESKey bdk, SecureDESKey kd2, byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderLmk
     * @param kd2
     * @param destinationPINBlockFormat
     * @return exported pin
     * @throws SMException
     */
    protected EncryptedPIN exportPINImpl (EncryptedPIN pinUnderLmk, SecureDESKey kd2,
            byte destinationPINBlockFormat) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNumber
     * @param pinLen
     * @param excludes
     * @return generated PIN under LMK
     * @throws SMException
     */
    protected EncryptedPIN generatePINImpl(String accountNumber, int pinLen, List<String> excludes) throws
            SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderLMK
     * @param pvkA
     * @param pvkB
     * @param pvkIdx
     * @return PVV (VISA PIN Verification Value)
     * @throws SMException 
     */
    protected String calculatePVVImpl(EncryptedPIN pinUnderLMK, SecureDESKey pvkA,
                       SecureDESKey pvkB, int pvkIdx) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd 
     * @param kd
     * @param pvkA
     * @param pvkB
     * @param pvki
     * @param pvv
     * @return
     * @throws SMException 
     */
    protected boolean verifyPVVImpl(EncryptedPIN pinUnderKd, SecureDESKey kd, SecureDESKey pvkA,
                        SecureDESKey pvkB, int pvki, String pvv) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderLmk
     * @param pvk
     * @param decTab
     * @param pinValData
     * @param minPinLen  pin minimal length
     * @return IBM PIN Offset
     * @throws SMException
     */
    protected String calculateIBMPINOffsetImpl(EncryptedPIN pinUnderLmk, SecureDESKey pvk,
                                            String decTab, String pinValData,
                                            int minPinLen) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param pinUnderKd
     * @param kd
     * @param pvk
     * @param offset
     * @param decTab
     * @param pinValData
     * @param minPinLen
     * @return
     * @throws SMException
     */
    protected boolean verifyIBMPINOffsetImpl(EncryptedPIN pinUnderKd, SecureDESKey kd
                            ,SecureDESKey pvk, String offset, String decTab
                            ,String pinValData, int minPinLen) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param pvk
     * @param decTab
     * @param pinValData
     * @param minPinLen
     * @param offset
     * @return derived PIN under LMK
     * @throws SMException
     */
    protected EncryptedPIN deriveIBMPINImpl(String accountNo, SecureDESKey pvk
                              ,String decTab, String pinValData, int minPinLen
                              ,String offset) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param cvkA
     * @param cvkB
     * @param expDate
     * @param serviceCode
     * @return Card Verification Code/Value
     * @throws SMException
     */
    protected String calculateCVVImpl(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                                   Date expDate, String serviceCode) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param accountNo
     * @param cvkA
     * @param cvkB
     * @param cvv
     * @param expDate
     * @param serviceCode
     * @return true if CVV/CVC is falid or false if not
     * @throws SMException
     */
    protected boolean verifyCVVImpl(String accountNo, SecureDESKey cvkA, SecureDESKey cvkB,
                        String cvv, Date expDate, String serviceCode) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }
    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param mkdm
     * @param skdm
     * @param imkac
     * @param accountNo
     * @param acctSeqNo
     * @param arqc
     * @param atc
     * @param upn
     * @param transData
     * @return true if ARQC/TC/AAC is falid or false if not
     * @throws SMException
     */
    protected boolean verifyARQCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accountNo, String acctSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, byte[] transData) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param mkdm
     * @param skdm
     * @param imkac
     * @param accountNo
     * @param acctSeqNo
     * @param arqc
     * @param atc
     * @param upn
     * @param arpcMethod
     * @param arc
     * @param propAuthData
     * @return calculated ARPC
     * @throws SMException
     */
    protected byte[] generateARPCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accountNo, String acctSeqNo, byte[] arqc, byte[] atc
            ,byte[] upn, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param mkdm
     * @param skdm
     * @param imkac
     * @param accountNo
     * @param acctSeqNo
     * @param arqc
     * @param atc
     * @param upn
     * @param arpcMethod
     * @param arc
     * @param propAuthData
     * @return Pair<Boolean,byte[]> of AC verification status and calculated ARPC
     * @throws SMException
     */
    protected Pair<Boolean,byte[]> verifyARQCGenerateARPCImpl(MKDMethod mkdm, SKDMethod skdm, SecureDESKey imkac
            ,String accountNo, String acctSeqNo, byte[] arqc, byte[] atc, byte[] upn
            ,byte[] transData, ARPCMethod arpcMethod, byte[] arc, byte[] propAuthData)
            throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param data
     * @param kd
     * @return generated CBC-MAC
     * @throws SMException
     */
    protected byte[] generateCBC_MACImpl (byte[] data, SecureDESKey kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Your SMAdapter should override this method if it has this functionality
     * @param data
     * @param kd
     * @return generated EDE-MAC
     * @throws SMException
     */
    protected byte[] generateEDE_MACImpl (byte[] data, SecureDESKey kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Translate key from encryption under the LMK held in “key change storage”
     * to encryption under a new LMK.
     *
     * @param kd the key encrypted under old LMK
     * @return key encrypted under the new LMK
     * @throws SMException if the parity of the imported key is not adjusted AND checkParity = true
     */
    protected SecureDESKey translateKeyFromOldLMKImpl (SecureDESKey kd) throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }

    /**
     * Erase the key change storage area of memory
     *
     * It is recommended that this command is used after keys stored
     * by the Host have been translated from old to new LMKs.
     *
     * @throws SMException
     */
    protected void eraseOldLMKImpl () throws SMException {
        throw  new SMException("Operation not supported in: " + this.getClass().getName());
    }
}



