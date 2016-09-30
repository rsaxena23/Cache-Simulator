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
        this.replace = new ReplacementSet(replacementPolicy, this.noOfSets, associativity);
    }

    public int findCacheEntry(int index, String tag)
    {
        //String[] tags = translateAddressToTag(addressValue);
        //int index = Integer.parseInt(tags[1]);
        for(int i=0;i<associativity;i++)
        {
            if(tagArray[index][i].loadedAddress.equals(tag))
                return i;
        }
        return -1;
    }

    public int getFreeBlock(int index)
    {
        for(int i=0;i<associativity;i++)
        {
            if(tagArray[index][i].loadedAddress.equals(null)) {
                /*Update Replace Structure*/
                return i;
            }
        }
        return -1;
    }

    public String[] translateAddressToTag(String address)
    {
        String[] tags=new String[3];
        int block_bits = (int)Math.log(blockSize);
        int index_bits = (int)Math.log(noOfSets);
        long addr = Long.parseLong(address,16);
        long val = Long.parseLong("ffffffffffffffff");

        tags[0] = (addr>>(block_bits+index_bits))+"";
        tags[1] = ( (addr&( val<<(index_bits+block_bits) ) )>>block_bits )+"";
        tags[2] = (addr&(val<<(block_bits)))+"";

        return tags;
    }

    public void updateEntry()
    {

    }
}
