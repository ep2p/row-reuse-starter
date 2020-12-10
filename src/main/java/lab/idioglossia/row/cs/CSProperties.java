package lab.idioglossia.row.cs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "row.cs")
@Getter
@Setter
public class CSProperties {
    private boolean reuse;
}
