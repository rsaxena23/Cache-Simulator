/**
 * Created by Raunaq on 28-09-2016.
 */
public class sim_cache {

    public static void main(String[] args)
    {
        if( args.length!=8 )
        {
            System.out.println("Not enough arguments to simulate Cache");
            System.exit(0);
        }

        int blocksize = Integer.parseInt(args[0]);
        int l1Size = Integer.parseInt(args[1]);
        int l1Assoc = Integer.parseInt(args[2]);
        int l2Size = Integer.parseInt(args[3]);
        int l2Assoc = Integer.parseInt(args[4]);
        int replacementPolicy = Integer.parseInt(args[5]);
        int l2InclusionPolicy = Integer.parseInt(args[6]);
        String traceFile = args[7];

        /* Enter Validation checks for size and creation */

        Cache L1 = new Cache(blocksize, l1Size, l1Assoc, replacementPolicy);
        Cache L2=null;
        if(l2Size!=0)
            L2 = new Cache(blocksize, l2Size, l2Assoc, replacementPolicy );


    }
}
