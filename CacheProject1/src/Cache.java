/**
 * Created by Raunaq-PC on 28-09-2016.
 */
public class Cache {
    int replacementPolicy;
    int blockSize;
    int totalSize;
    int associativity;
    public Cache(int blockSize,int totalSize,int associativity,int replacementPolicy)
    {
        this.replacementPolicy = replacementPolicy;
        this.blockSize = blockSize;
        this.totalSize = totalSize;
        this.associativity = associativity;
    }
}
