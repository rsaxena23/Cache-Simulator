import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by Raunaq on 28-09-2016.
 */
public class sim_cache {

    static Cache L1=null,L2=null;
    static int memBlocks=0;                 /* Number of blocks transferred to and from memory */

    public  void startSimulate(String traceFile)
    {
        try {
            BufferedReader br = new BufferedReader( new FileReader(traceFile) );
            String line,address;
            char operation;
            while((line = br.readLine())!=null)
            {
                operation = line.substring( 0, line.indexOf(' ')).charAt(0);
                address = line.substring(line.indexOf(' ')+1);
                executeOperation(operation,address);
            }

            /* Stats check */
            System.out.println("===== Simulation results (raw) =====");
            System.out.println("a. number of L1 reads:        "+(L1.log.readHits+L1.log.readMisses));
            System.out.println("b. number of L1 read misses:  "+(L1.log.readMisses));
            System.out.println("c. number of L1 writes:       "+(L1.log.writeHits+L1.log.writeMisses));
            System.out.println("d. number of L1 write misses: "+(L1.log.writeMisses));

            double l1MissRate = (double)( L1.log.readMisses + L1.log.writeMisses  ) /(double) (L1.log.readHits+L1.log.readMisses+L1.log.writeHits+L1.log.writeMisses );

            System.out.println("e. L1 Miss rate:              "+l1MissRate);
            System.out.println("f. number of L1 writebacks:   "+(L1.log.writeBacks));

          /*  System.out.println("Reads- "+(L1.log.readHits+L1.log.readMisses)+" Misses:"+L1.log.readMisses);
            System.out.println("Writes- "+(L1.log.writeHits+L1.log.writeMisses)+" Misses:"+L1.log.writeMisses);
            System.out.println("WriteBacks - "+(L1.log.writeBacks)); */
            if(L2!=null) {

                System.out.println("g. number of L2 reads:        "+(L2.log.readHits+L2.log.readMisses));
                System.out.println("h. number of L2 read misses:  "+(L2.log.readMisses));
                System.out.println("i. number of L2 writes:       "+(L2.log.writeHits+L2.log.writeMisses));
                System.out.println("j. number of L2 write misses: "+(L2.log.writeMisses));

                double l2MissRate = (double)( L2.log.readMisses + L2.log.writeMisses  ) /(double) (L2.log.readHits+L2.log.readMisses+L2.log.writeHits+L2.log.writeMisses );

                System.out.println("k. L2 Miss rate:              "+l2MissRate);
                System.out.println("l. number of L2 writebacks:   "+(L2.log.writeBacks));

                /*System.out.println("Reads- " + (L2.log.readHits + L2.log.readMisses) + " Misses:" + L2.log.readMisses);
                System.out.println("Writes- " + (L2.log.writeHits + L2.log.writeMisses) + " Misses:" + L2.log.writeMisses);
                System.out.println("WriteBacks - "+(L2.log.writeBacks));*/
            }
            else
            {
                int zero=0;
                System.out.println("g. number of L2 reads:        "+(zero));
                System.out.println("h. number of L2 read misses:  "+(zero));
                System.out.println("i. number of L2 writes:       "+(zero));
                System.out.println("j. number of L2 write misses: "+(zero));
                System.out.println("k. L2 Miss rate:              "+(float)zero);
                System.out.println("l. number of L2 writebacks:   "+(zero));
            }

            System.out.println("m. total memory traffic:      "+memBlocks);

        }catch(IOException ie) {
            System.out.println("Exception:"+ ie.getMessage());
        }
    }

    public void executeOperation(char operation,String address)
    {
        String[] l1Tags = L1.translateAddressToTag(address);
        int l1Index = Integer.parseInt(l1Tags[1]);
        int l1Find=L1.findCacheEntry(l1Index,l1Tags[0]);
        /*Check if L1 Hit */
        if(l1Find!=Constants.NO)
        {
            if(operation=='r')
                L1.log.readHits++;
            else {
                L1.log.writeHits++;
                L1.tagArray[l1Index][l1Find].isDirty=true;
            }
            L1.replace.updateIndex(l1Index,l1Find);
            return;
        }
        /*Check L2 if present */
        else if(L2!=null)
        {
            String[] l2Tags = L2.translateAddressToTag(address);
            int l2Index = Integer.parseInt(l2Tags[1]);
            int l2Find = L2.findCacheEntry(l2Index,l2Tags[0]);

            if (l2Find!=Constants.NO)
            {
                //if(operation=='r')
                    L2.log.readHits++;
                /*else {
                    L2.log.writeHits++;
                    L2.tagArray[l2Index][l2Find].isDirty=true;
                } */
                L2.replace.updateIndex(l2Index,l2Find);
                if(L2.inclusiveness == Constants.EXCLUSIVE)
                {
                    if(L2.tagArray[l2Index][l2Find].isDirty)
                    {
                        memBlocks++;
                        L2.log.writeBacks++;
                    }

                    L2.tagArray[l2Index][l2Find].isDirty=false;
                    L2.tagArray[l2Index][l2Find].completeAddress=null;
                    L2.tagArray[l2Index][l2Find].loadedAddress=null;
                }
            }
            else {
                //if(operation=='r')
                    L2.log.readMisses++;
                //else
                  //  L2.log.writeMisses++;

                /* Since Don't have to load in L2 for exclusive */
                if(L2.inclusiveness!=Constants.EXCLUSIVE) {
                    int l2Way = L2.getFreeBlock(l2Index);
                    if (l2Way == Constants.NO) {
                        l2Way = L2.replace.getReplaceIndex(l2Index);
                        if(L2.inclusiveness==Constants.INCLUSIVE) {
                            String[] tempL1Tags = L1.translateAddressToTag(L2.tagArray[l2Index][l2Way].completeAddress);
                            int tempL1Index = Integer.parseInt(tempL1Tags[0]);
                            int tempL1Way = L1.findCacheEntry(tempL1Index, tempL1Tags[0]);
                            if (tempL1Way!=Constants.NO)
                            {
                                if(L1.tagArray[tempL1Index][tempL1Way].isDirty)
                                {
                                    memBlocks++;
                                    L1.log.writeBacks++;  // Check?
                                    /* Reset the block */
                                    L1.tagArray[tempL1Index][tempL1Way].isDirty=false;
                                    L1.tagArray[tempL1Index][tempL1Way].completeAddress=null;
                                    L1.tagArray[tempL1Index][tempL1Way].loadedAddress=null;
                                }
                            }
                        }
                    }
                    else
                        L2.replace.updateIndex(l2Index, l2Way);

                    if (L2.tagArray[l2Index][l2Way].isDirty) {
                        L2.log.writeBacks++;
                        memBlocks++;
                    }

                    L2.tagArray[l2Index][l2Way].loadedAddress = l2Tags[0];
                    L2.tagArray[l2Index][l2Way].completeAddress = address;
                    if (operation == 'r')
                        L2.tagArray[l2Index][l2Way].isDirty = false;
                    else
                        L2.tagArray[l2Index][l2Way].isDirty = true;
                    memBlocks++;
                }
            }
        }

        /* L1 Miss */

        if(operation=='r')
            L1.log.readMisses++;
        else
            L1.log.writeMisses++;

        if(L2==null)
            memBlocks++;

        int l1Way = L1.getFreeBlock(l1Index);
        if(l1Way == Constants.NO) {
            l1Way = L1.replace.getReplaceIndex(l1Index);
            if(L2.inclusiveness==Constants.EXCLUSIVE &&  !L1.tagArray[l1Index][l1Way].isDirty)
            {
                String[] tempL2Tags = L2.translateAddressToTag(L1.tagArray[l1Index][l1Way].completeAddress);
                int tempL2Index = Integer.parseInt(tempL2Tags[1]);
                int tempL2Way = L2.getFreeBlock(tempL2Index);
                if(tempL2Way==Constants.NO)
                {
                    tempL2Way = L2.replace.getReplaceIndex(tempL2Index);
                    if(L2.tagArray[tempL2Index][tempL2Way].isDirty) {
                        memBlocks++;
                        L2.log.writeBacks++;
                    }
                }
                L2.tagArray[tempL2Index][tempL2Way].completeAddress = address;
                L2.tagArray[tempL2Index][tempL2Way].loadedAddress = tempL2Tags[0];
                L2.tagArray[tempL2Index][tempL2Way].isDirty=true;
                L2.log.writeMisses++;

            }
        }
        else
            L1.replace.updateIndex(l1Index,l1Way);

        if(L1.tagArray[l1Index][l1Way].isDirty) {

            L1.log.writeBacks++;
            if(L2!=null)
            {
                String[] tempL2Tags = L2.translateAddressToTag(L1.tagArray[l1Index][l1Way].completeAddress);
                int tempL2Index = Integer.parseInt(tempL2Tags[1]);
                int tempL2Find = L2.findCacheEntry(tempL2Index , tempL2Tags[0] );
                if(tempL2Find!=Constants.NO)
                {
                        L2.log.writeHits++;
                        L2.tagArray[tempL2Index][tempL2Find].isDirty=true;
                    if(L2.inclusiveness == Constants.EXCLUSIVE)
                    {
                        if(L2.tagArray[tempL2Index][tempL2Find].isDirty)
                        {
                            memBlocks++;
                            L2.log.writeBacks++;
                        }
                        L2.tagArray[tempL2Index][tempL2Find].isDirty=false;
                        L2.tagArray[tempL2Index][tempL2Find].completeAddress=null;
                        L2.tagArray[tempL2Index][tempL2Find].loadedAddress=null;
                    }
                }
                else
                {
                    int tempL2Way = L2.getFreeBlock(tempL2Index);
                    if(tempL2Way==Constants.NO) {
                        tempL2Way = L2.replace.getReplaceIndex(tempL2Index);
                        if(L2.inclusiveness==Constants.INCLUSIVE) {
                            String[] tempL1Tags = L1.translateAddressToTag(L2.tagArray[tempL2Index][tempL2Way].completeAddress);
                            int tempL1Index = Integer.parseInt(tempL1Tags[0]);
                            int tempL1Way = L1.findCacheEntry(tempL1Index, tempL1Tags[0]);
                            if (tempL1Way!=Constants.NO)
                            {
                                if(L1.tagArray[tempL1Index][tempL1Way].isDirty)
                                {
                                    memBlocks++;
                                    L1.log.writeBacks++;  // Check?
                                    /* Reset the block */
                                    L1.tagArray[tempL1Index][tempL1Way].isDirty=false;
                                    L1.tagArray[tempL1Index][tempL1Way].completeAddress=null;
                                    L1.tagArray[tempL1Index][tempL1Way].loadedAddress=null;
                                }
                            }
                        }
                    }
                    else
                        L2.replace.updateIndex(tempL2Index,tempL2Way);

                    if(L2.tagArray[tempL2Index][tempL2Way].isDirty) {
                        //L2.log.writeBacks++;
                        memBlocks++;
                    }
                    //int tempL2Way = L2.replace.getReplaceIndex(tempL2Index);
                    //L2.replace.updateIndex(tempL2Index,tempL2Way);
                    L2.log.writeMisses++;
                    L2.tagArray[tempL2Index][tempL2Way].isDirty=true;
                    L2.tagArray[tempL2Index][tempL2Way].loadedAddress = tempL2Tags[0];
                    L2.tagArray[tempL2Index][tempL2Way].completeAddress = L1.tagArray[l1Index][l1Way].completeAddress;
                    L2.log.writeBacks++;
                    //memBlocks++;
                }
            }
            else
                memBlocks++;
        }

        L1.tagArray[l1Index][l1Way].loadedAddress = l1Tags[0];
        L1.tagArray[l1Index][l1Way].completeAddress = address;

        if(operation=='r')
            L1.tagArray[l1Index][l1Way].isDirty = false;
        else
            L1.tagArray[l1Index][l1Way].isDirty = true;
    }

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

        L1 = new Cache(blocksize, l1Size, l1Assoc, replacementPolicy);
        L2=null;
        if(l2Size!=0) {
            L2 = new Cache(blocksize, l2Size, l2Assoc, replacementPolicy);
            L2.inclusiveness = l2InclusionPolicy;
        }

        /* Print config*/
        System.out.println("===== Simulator configuration =====");
        System.out.println("BLOCKSIZE:             "+blocksize+"");
        System.out.println("L1_SIZE:               "+l1Size);
        System.out.println("L1_ASSOC:              "+l1Assoc);
        System.out.println("L2_SIZE:               "+l2Size);
        System.out.println("L2_ASSOC:              "+l2Assoc);
        String replaceStr="",inclusionStr="";
        switch(replacementPolicy){
            case 0:
                replaceStr="LRU";
                break;
            case 1:
                replaceStr="FIFO";
                break;
            case 2:
                replaceStr="PSEUDO";
                break;
            case 3:
                replaceStr="OPTIMAL";
                break;
        }
        switch(l2InclusionPolicy){
            case 0:
                inclusionStr="non inclusive";
                break;
            case 1:
                inclusionStr="inclusive";
                break;
            case 2:
                inclusionStr="exclusive";
                break;
        }
        System.out.println("REPLACEMENT POLICY:    "+replaceStr);
        System.out.println("INCLUSION PROPERTY:    "+inclusionStr);
        System.out.println("trace_file:            "+traceFile);




        new sim_cache().startSimulate(traceFile);
    }
}
