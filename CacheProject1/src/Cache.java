/**
 * Created by Raunaq on 28-09-2016.
 */
public class Cache {
    int replacementPolicy;
    int blockSize;
    int totalSize;
    int associativity;
    int noOfSets;
    CacheEntry tagArray[][];

    public Cache(int blockSize,int totalSize,int associativity,int replacementPolicy)
    {
        this.replacementPolicy = replacementPolicy;
        this.blockSize = blockSize;
        this.totalSize = totalSize;
        this.associativity = associativity;
        this.noOfSets = totalSize/(blockSize*associativity);
        this.tagArray = new CacheEntry[this.noOfSets][associativity];
    }

    public boolean checkCacheEntry(String addressValue,int index)
    {
        for(int i=0;i<associativity;i++)
        {
            if(tagArray[index][associativity].equals(addressValue))
                return true;
        }
        return false;
    }

    public void updateEntry()
    {

    }
}
