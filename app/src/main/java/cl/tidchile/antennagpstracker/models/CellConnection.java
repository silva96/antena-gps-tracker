package cl.tidchile.antennagpstracker.models;

/**
 * Created by benjamin on 3/3/16.
 */
public class CellConnection {
    private String network_type;
    //GSM VALUES
    private int cid;
    private int lac;
    //LTE VALUES
    private int ci;
    private int pci;
    private int tac;
    //COMMON
    //signal strength asu level
    private int ss;
    private int ssl;
    private boolean is_registered;


    public CellConnection(int pci, int ci, int tac, int ss, int ssl, boolean is_registered, String network_type) {
        this.pci = pci;
        this.ci = ci;
        this.tac = tac;
        this.ss = ss;
        this.ssl = ssl;
        this.network_type = network_type;
        this.is_registered = is_registered;
    }
    public CellConnection(int cid, int lac, int ss, int ssl, boolean is_registered, String network_type) {
        this.cid = cid;
        this.lac = lac;
        this.ss = ss;
        this.ssl = ssl;
        this.network_type = network_type;
        this.is_registered = is_registered;

    }
    public CellConnection(int cid, int lac, boolean is_registered, String network_type){
        this.cid = cid;
        this.lac = lac;
        this.network_type = network_type;
        this.is_registered = is_registered;
    }

    public String getNetwork_type() {
        return network_type;
    }

    public void setNetwork_type(String network_type) {
        this.network_type = network_type;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getLac() {
        return lac;
    }

    public void setLac(int lac) {
        this.lac = lac;
    }

    public int getCi() {
        return ci;
    }

    public void setCi(int ci) {
        this.ci = ci;
    }

    public int getPci() {
        return pci;
    }

    public void setPci(int pci) {
        this.pci = pci;
    }

    public int getTac() {
        return tac;
    }

    public void setTac(int tac) {
        this.tac = tac;
    }

    public int getSs() {
        return ss;
    }

    public void setSs(int ss) {
        this.ss = ss;
    }

    public int getSsl() {
        return ssl;
    }

    public void setSsl(int ssl) {
        this.ssl = ssl;
    }

    public boolean is_registered() {
        return is_registered;
    }
}
