package br.com.erudio.file.exporter.impl;

import br.com.erudio.data.dto.v1.PersonDTO;
import br.com.erudio.file.exporter.contract.FileExporter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class XlsxExporter implements FileExporter {
    @Override
    public Resource exportFile(List<PersonDTO> people) throws Exception {
        return null;
    }
}
