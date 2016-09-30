/**
 * Created by Raunaq on 28-09-2016.
 */
public class Cache {
    ReplacementSet replace;
    int blockSize;
    int totalSize;
    int associativity;
    int noOfSets;
    CacheEntry tagArray[][];
    Metrics log;
    int inclusiveness;

    public Cache(int blockSize,int totalSize,int associativity,int replacementPolicy)
    {
        this.blockSize = blockSize;
        this.totalSize = totalSize;
        this.associativity = associativity;
        this.noOfSets = totalSize/(blockSize*associativity);
        this.tagArray = new CacheEntry[this.noOfSets][associativity];
        for(int i=0;i<this.noOfSets;i++)
            for(int j=0;j<associativity;j++)
                tagArray[i][j]= new CacheEntry();
        this.replace = new ReplacementSet(replacementPolicy, this.noOfSets, associativity);
        this.log = new Metrics();
    }

    public int findCacheEntry(int index, String tag)
    {
        //String[] tags = translateAddressToTag(addressValue);
        //int index = Integer.parseInt(tags[1]);
        if(tag==null)
            return -1;

        for(int i=0;i<associativity;i++)
        {
            if( tagArray[index][i].loadedAddress!=null &&  tagArray[index][i].loadedAddress.equals(tag))
                return i;
        }
        return -1;
    }

    public int getFreeBlock(int index)
    {
        for(int i=0;i<associativity;i++)
        {
            if(tagArray[index][i].loadedAddress==null) {
                /*Update Replace Structure*/
                return i;
            }
        }
        return -1;
    }

    public String[] translateAddressToTag(String address)
    {
        String[] tags=new String[3];
        int block_bits = (int)(Math.log(blockSize)/Math.log(2));
        int index_bits = (int)(Math.log(noOfSets)/Math.log(2));

        long addr = Long.parseLong(address,16);
        long val1 =(long) (Math.pow(2,(index_bits+block_bits)) - 1);
        long val2 = (long) (Math.pow(2,(block_bits)) - 1);

        tags[0] = Long.toHexString( addr>>(block_bits+index_bits) );
        tags[1] = ( (addr&( val1) )>>block_bits )+"";
        tags[2] = (addr&(val2))+"";
       //System.out.println("Tags:"+tags[0]+" "+tags[1]+" "+tags[2] );
        return tags;
    }

    public void updateEntry()
    {

    }
}
