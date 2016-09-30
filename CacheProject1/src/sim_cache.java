import com.sun.org.apache.xalan.internal.xsltc.compiler.Pattern;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.ContentModelContainer;

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
            System.out.println("L1:");
            System.out.println("Reads- Hits:"+(L1.log.readHits+L1.log.readMisses)+" Misses:"+L1.log.readMisses);
            System.out.println("Writes- Hits:"+(L1.log.writeHits+L1.log.writeMisses)+" Misses:"+L1.log.writeMisses);

            System.out.println("L2:");
            //System.out.println("Reads- Hits:"+L2.log.readHits+" Misses:"+L2.log.readMisses);
            //System.out.println("Writes- Hits:"+L2.log.writeHits+" Misses:"+L2.log.writeMisses);

            System.out.println("Memory traffic:"+memBlocks);

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
                if(operation=='r')
                    L2.log.readHits++;
                else {
                    L2.log.writeHits++;
                    L2.tagArray[l1Index][l1Find].isDirty=true;
                }
                L2.replace.updateIndex(l2Index,l2Find);
            }
            else {
                if(operation=='r')
                    L2.log.readMisses++;
                else
                    L2.log.writeMisses++;

                int l2Way = L2.getFreeBlock(l2Index);
                if(l2Way==Constants.NO)
                    l2Way = L2.replace.getReplaceIndex(l2Index);
                L2.replace.updateIndex(l2Index,l2Way);
                if(L2.tagArray[l1Index][l2Way].isDirty)
                    memBlocks++;

                L2.tagArray[l2Index][l2Way].loadedAddress = l2Tags[0];
                L2.tagArray[l2Index][l2Way].completeAddress = address;
                if(operation=='r')
                    L2.tagArray[l2Index][l2Way].isDirty = false;
                else
                    L2.tagArray[l2Index][l2Way].isDirty = true;
                memBlocks++;
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
        if(l1Way== Constants.NO)
            l1Way = L1.replace.getReplaceIndex(l1Index);

        L1.replace.updateIndex(l1Index,l1Way);

        if(L1.tagArray[l1Index][l1Way].isDirty) {
            memBlocks++;
            if(L2!=null && L2.inclusiveness!=Constants.EXCLUSIVE)
            {
                String[] tempL2Tags = L2.translateAddressToTag(L1.tagArray[l1Index][l1Way].completeAddress);
                int tempL2Index = Integer.parseInt(tempL2Tags[1]);
                int tempL2Find = L2.findCacheEntry(tempL2Index , tempL2Tags[0] );
                if(tempL2Find!=Constants.NO)
                    L2.tagArray[tempL2Index][tempL2Find].isDirty= false;
            }
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

        new sim_cache().startSimulate(traceFile);
    }
}
