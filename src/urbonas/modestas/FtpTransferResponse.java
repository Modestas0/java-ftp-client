package urbonas.modestas;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class FtpTransferResponse extends FtpResponse {
    private final byte[] data;

    public FtpTransferResponse(FtpResponse response, byte[] data) {
        super(response.getStatus(), response.getMessages());
        this.data = data;
    }

    public FtpTransferResponse(int status, List<String> messages, byte[] data) {
        super(status, messages);
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        String dataAsStr;
        try {
            dataAsStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            dataAsStr = new String(data, Charset.defaultCharset());
        }
        return "FtpTransferResponse {" + super.toString() + ", " + dataAsStr + " }";
    }
}
