import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * static service function class
 */
public class Function
{

    public static String getMD5(char[] array)
    {
        String str = new String(array), hashtext = "";
        byte[] bytesOfMessage = null;
        byte[] thedigest = null;

        try
        {

            bytesOfMessage = str.getBytes("UTF-8");
            str = "";

            MessageDigest md = MessageDigest.getInstance("MD5");
            thedigest = md.digest(bytesOfMessage);

            BigInteger bigInt = new BigInteger(1, thedigest);
            hashtext = bigInt.toString(16);

            while (hashtext.length() < 32)
            {
                hashtext = "0" + hashtext;
            }

        }
        catch (UnsupportedEncodingException e)
        {
            System.out.println("Your machine does not support UTF-8 encoding: " + e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            System.out.println("Your machine does not support MD5 hashing: " + e.getMessage());
        }

        return hashtext;
    }

    public static String getMD5(String str)
    {
        String hashtext = "";
        byte[] bytesOfMessage = null;
        byte[] thedigest = null;

        try
        {

            bytesOfMessage = str.getBytes("UTF-8");
            str = "";

            MessageDigest md = MessageDigest.getInstance("MD5");
            thedigest = md.digest(bytesOfMessage);

            BigInteger bigInt = new BigInteger(1, thedigest);
            hashtext = bigInt.toString(16);

            while (hashtext.length() < 32)
            {
                hashtext = "0" + hashtext;
            }

        }
        catch (UnsupportedEncodingException e)
        {
            System.out.println("Your machine does not support UTF-8 encoding: " + e.getMessage());
        }
        catch (NoSuchAlgorithmException e)
        {
            System.out.println("Your machine does not support MD5 hashing: " + e.getMessage());
        }

        return hashtext;
    }
}
