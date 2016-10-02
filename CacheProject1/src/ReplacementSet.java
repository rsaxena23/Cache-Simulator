import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;

/**
 * Created by Raunaq-PC on 29-09-2016.
 */
public class ReplacementSet {
    int[][] replData;
    int replacementPolicy;
    int systemTimer=0;
    int ways;

    public ReplacementSet(int replacementPolicy,int noOfSets,int associativity)
    {
        this.ways = associativity;
        this.replacementPolicy=replacementPolicy;
        if(replacementPolicy == Constants.LRU)
            replData = new int[noOfSets][associativity];
        else if(replacementPolicy == Constants.FIFO)
            replData = new int[noOfSets][1];
        else if(replacementPolicy == Constants.PSEUDO_LRU)
            replData = new int [noOfSets][associativity - 1];
    }
    public int getReplaceIndex(int index)
    {
        /* LRU Case*/
        if(replacementPolicy== Constants.LRU)
        {
            int min = replData[index][0];
            int replace_index=0;
            for(int i=1;i<replData[index].length;i++) {
                if (replData[index][i] < min) {
                    min = replData[index][i];
                    replace_index=i;
                }
            }
            replData[index][replace_index] = ++systemTimer;
            return replace_index;
        }

        /* FIFO Case */
        else if(replacementPolicy==Constants.FIFO) {

            return replData[index][0]++%ways;
        }

        /*Pseudo LRU Case */
        else if(replacementPolicy==Constants.PSEUDO_LRU)
        {
            String binaryIndexValue="";
            int i= 1,replace_index;
            while(i<ways)
            {
                binaryIndexValue+=replData[index][i-1];
                i=(i*2) + replData[index][i-1];
            }
            replace_index = Integer.parseInt(binaryIndexValue,2);
            //System.out.println(replace_index);
            updateIndex(index,replace_index);
            return replace_index;
        }

        return -1;
    }
    public void updateIndex(int index, int way)
    {
        if(replacementPolicy==Constants.LRU)
            replData[index][way]= ++systemTimer;

        else if(replacementPolicy==Constants.PSEUDO_LRU)
        {
            int ways=this.ways -1;
            //System.out.println("A:"+index+"-"+way+" "+replData[index][0]+","+replData[index][1]+","+replData[index][2]);
            if(replData[index][((ways)/2) + (way/2)]==0 && way%2==0 )
                replData[index][ (ways/2) + (way/2)  ] = 1;
            else if(replData[index][(ways/2) + (way/2)]==1 && way%2==1) {
                replData[index][ (ways/2) + (way/2)  ] = 0;
                int i = ((ways/2) + (way/2) - (way%2))/2 + 1;
                while (i > 0) {
                    if(replData[index][ i - 1 ] == 0) {
                        replData[index][(i-1)] = 1;
                        break;
                    }
                    else
                    {
                        replData[index][(i-1)] = 0;
                        i=i/2;
                    }
                }
            }
        }
        //System.out.println("B:"+index+"-"+way+" "+replData[index][0]+","+replData[index][1]+","+replData[index][2]);
    }
}
