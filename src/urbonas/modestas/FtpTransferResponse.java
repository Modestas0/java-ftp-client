package urbonas.modestas;

import java.util.List;

public class FtpTransferResponse extends FtpResponse {
    private final byte[] data;

    public FtpTransferResponse(int status, List<String> messages, byte[] data) {
        super(status, messages);
        this.data = data;
    }
}