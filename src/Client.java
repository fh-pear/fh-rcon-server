import java.util.List;
import java.util.ArrayList;
import java.util.regex.*;

public class Client
{

    private String cid, name, guid, score, dataid;
    private int level;
    boolean guidValid = true; // assume true, checkGuid() will set false if otherwise
    private String exactName = "";
    //private Database db;

    /**
     * Client object constructor. Creates a Client object based on a single line
     * from a rcon "status" command. Parses out the guid, name, and client id.
     *
     * @param str The string will be a single line from a rcon status command.
     * Example line: 9 70 309 aaaa1234567890bbbbbbcccccccddddd 77solor^7 0
     * 75.35.141.4:9704
     */
    public Client(String str) throws IllegalArgumentException
    {
        if (str.isEmpty())
            throw new IllegalArgumentException("Provided string is empty");
        //System.out.println("Client input: " + str);
        /* create a client object. string will look ssomething like the following three examples:
   	     9    70  309 aaaa1234567890bbbbbbcccccccddddd 77solor^7               0 75.35.141.4:9704      15390 25000
          
   		 10     0  163 dddddeeeeeeffffff1234567890abcde Ben.AC^7                0 57.155.157.209:28960  -21538 25000
    
          11    75   50 aaaabbbbccccddddeeeeffff12345678 Squeaky^7               0 74.241.144.180:28960  11964 25000
         */
        String st[] = str.split(" ");
        ArrayList<String> split = new ArrayList<String>();

        for (int i = 0; i < st.length; i++)
        {
            if (st[i].equals(""));
            else
            {
                split.add(st[i]);
                //System.out.println("Index " + i +": \t" + st[i]);
            }
        }
        split.trimToSize();
        //System.out.println(split.size());

        // build the client attributes
        /* normally, split[] will have 9 elements. if it has more, then there are spaces in the client name */
        if (split.size() == 9 || split.size() == 8)
        {
            //System.out.println("no spaces");
            cid = split.get(0);
            score = split.get(1);
            //System.out.println(cid);
            guid = split.get(3);
            exactName = split.get(4);

            if (exactName.equals("^7"))
            {
                exactName = "UnnamedPlayer^7";
            }
        }
        else // name has spaces
        {
            int extra = split.size() - 9;
            //System.out.println("spaces");
            //System.out.println(split.size());
            cid = split.get(0);
            score = split.get(1);
            guid = split.get(3);
            //System.out.println("extra: " + extra);

            for (int i = 4; i <= 4 + extra; i++)
            {
                exactName = exactName + split.get(i) + " ";
            }

            if (exactName.equals("^7"))
            {
                exactName = "UnnamedPlayer^7";
            }
        }

        checkGuid();
        makeName();
        // getClientDetails();
    }

    /**
     * Client object constructor This constructor is for when the variables have
     * already been parsed from a rcon reply. Use case ex creating a deep copy
     *
     * @param id will be the cid
     * @param s will be the score
     * @param n will be the name
     * @param eN becomes the exactName
     * @param g becomes the guid
     */
    public Client(String id, String s, String n, String eN, String g)
    {
        cid = id;
        score = s;
        name = n;
        exactName = eN;
        guid = g;

        //  getClientDetails();
    }

    /* public void getClientDetails()
   {
      db = new Database();
   	
      db.getDetails(guid);
      ArrayList<String> r = new ArrayList<String>(db.getResults());
   	
		try {
		System.out.println("getting dbid for client: " + r.get(0));
      dataid = r.get(0);
   	
      switch (Integer.parseInt(r.get(1))) {
         case 2048: // leaders
            level = 100;
            break;
         case 1024: // executive admin
            level = 90;
            break;
         case 512: // senior admin
            level = 80;
            break;
         case 256: // asst senior staff
            level = 69;
            break;
         case 128: // full admin
            level = 60;
            break;
         case 64: // admin
            level = 40;
            break;
         case 32: // moderator
            level = 20;
            break;
         case 16: // these are levels that are below moderators (mods are groupbits = 32). below mod should not be able to login
            level = 15;
				break;
			case 8:
				level = 10;
				break;
			case 2:
				level = 2;
				break;
			case 1:
				level = 1;
				break;
			default:
				level = 0;
				break;
      	}
			
      }
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		catch (IndexOutOfBoundsException e) {
			// client is not in the database yet
			level = 0;
			dataid = null;
		}
   }*/
    public String toString(boolean fullDetails)
    {
        if (fullDetails)
        {
            String str = cid + "\t"
                    + score + "\t"
                    + name + "\t"
                    + exactName + "\t"
                    + guid + "\n";

            return str;
        }
        else
        {
            String str = cid + "\t"
                    + score + "\t"
                    + name + "\t"
                    + exactName + "\t"
                    + guid.substring(guid.length() - 8) + "\n";

            return str;
        }
    }

    /**
     * checkGuid() will check guid for any irregularities. prevents malfromed
     * guids. If a malformed guid is found, calls the kickClient() method to
     * invoke a rcon kick command. Guid spoofing is a common problem. This
     * method will help combat some of it by detecting and kicking malformed
     * guids
     */
    private void checkGuid()
    {
        if (guid.length() != 32)
        {
            guidValid = false;
        }

        Pattern pattern = Pattern.compile("[g-z][G-Z]");
        Matcher matcher = pattern.matcher(guid);

        if (matcher.find())
        {
            guidValid = false;
        }
    }

    public boolean guidIsValid()
    {
        return guidValid;
    }

    /**
     * makeName() builds the name field with no in-game color characters
     */
    private void makeName()
    {
        name = exactName;
        name = name.replaceAll("\\^0", "");
        name = name.replaceAll("\\^1", "");
        name = name.replaceAll("\\^2", "");
        name = name.replaceAll("\\^3", "");
        name = name.replaceAll("\\^4", "");
        name = name.replaceAll("\\^5", "");
        name = name.replaceAll("\\^6", "");
        name = name.replaceAll("\\^7", "");
        name = name.replaceAll("\\^8", "");
        name = name.replaceAll("\\^9", "");
    }

    /**
     * guid accessor
     *
     * @return the guid field
     */
    public String getGuid()
    {
        return guid;
    }

    public String getShortGuid()
    {
        return guid.substring(guid.length() - 8);
    }

    /**
     * exactName accessor
     *
     * @return the exactName field
     */
    public String getExactName()
    {
        return exactName;
    }

    /**
     * name accessor
     *
     * @return the name field
     */
    public String getName()
    {
        return name;
    }

    /**
     * cid accessor
     *
     * @return the cid field
     */
    public String getClientId()
    {
        return cid;
    }

    public String getScore()
    {
        return score;
    }

    /**
     * equals() method used to compare two client objects
     *
     * @param c Client object to compare against
     *
     * @return true if the objects are equal, false if not
     */
    public boolean equals(Client c)
    {
        if (guid.equals(c.getGuid()) && exactName.equals(c.getExactName()) && cid.equals(c.getClientId()))
        {
            return true;
        }

        return false;
    }

    public String print()
    {
        String str = cid + " " + name + " " + guid + " " + exactName;
        return str;
    }

}
