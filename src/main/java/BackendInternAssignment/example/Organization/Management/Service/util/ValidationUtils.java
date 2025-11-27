package BackendInternAssignment.example.Organization.Management.Service.util;

import BackendInternAssignment.example.Organization.Management.Service.exception.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    private ValidationUtils() {
    }

    public static void requireNonBlank(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new BadRequestException(fieldName + " is required.");
        }
    }

    public static void requireEmail(String value, String fieldName) {
        requireNonBlank(value, fieldName);
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new BadRequestException("Invalid email format for " + fieldName + ".");
        }
    }

    public static void requireMinLength(String value, int minLength, String fieldName) {
        requireNonBlank(value, fieldName);
        if (value.trim().length() < minLength) {
            throw new BadRequestException(fieldName + " must be at least " + minLength + " characters long.");
        }
    }
}

