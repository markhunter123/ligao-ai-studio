package ligao.exception;

import lombok.Getter;
import ligao.common.result.ResultCode;

/**
 * Ligao 业务异常
 *
 * @author Zhouyin.Wei
 */
@Getter
public class LigaoException extends RuntimeException {

    private final int code;
    /** i18n message key (optional). When set, GlobalExceptionHandler uses this to look up translated message. */
    private final String msgKey;

    public LigaoException(String message) {
        super(message);
        this.code = 500;
        this.msgKey = null;
    }

    public LigaoException(int code, String message) {
        super(message);
        this.code = code;
        this.msgKey = null;
    }

    public LigaoException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
        this.msgKey = null;
    }

    /**
     * Create with i18n message key. The key is resolved by GlobalExceptionHandler.
     * @param msgKey i18n key (e.g. "err.workspace.not_found")
     * @param message fallback message (Chinese or default text)
     */
    public LigaoException(String msgKey, String message) {
        super(message);
        this.code = 500;
        this.msgKey = msgKey;
    }

    /**
     * Create with i18n message key and custom code.
     */
    public LigaoException(String msgKey, int code, String message) {
        super(message);
        this.code = code;
        this.msgKey = msgKey;
    }
}
