package sopt.comfit.global.logging;

import org.slf4j.MDC;

import java.util.UUID;

public class MdcUtils {

    public static final String TRACE_ID = "traceId";
    public static final String JOB_ID = "jobId";
    public static final String USER_ID = "userId";

    private MdcUtils() {
    }

    public static void generateTraceId() {
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put(TRACE_ID, traceId);
    }

    public static void setJobId(Long jobId) {
        MDC.put(JOB_ID, String.valueOf(jobId));
    }

    public static void setUserId(Long userId) {
        MDC.put(USER_ID, String.valueOf(userId));
    }

    public static void clear() {
        MDC.clear();
    }
}