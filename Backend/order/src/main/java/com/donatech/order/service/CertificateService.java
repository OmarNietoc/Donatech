package com.donatech.order.service;

import com.donatech.order.dto.CompanyDetailsDto;
import com.donatech.order.model.CertificateConfig;
import com.donatech.order.model.Order;
import com.donatech.order.model.OrderItem;
import com.donatech.order.repository.CertificateConfigRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Genera el PDF del certificado de donación (HTML → PDF con openhtmltopdf).
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateConfigRepository certificateConfigRepository;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public byte[] generate(Order order, CompanyDetailsDto company, String campaignTitulo) {
        CertificateConfig cfg = certificateConfigRepository.findById(1L).orElse(null);
        String html = buildHtml(order, company, campaignTitulo, cfg);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo generar el certificado de donación: " + e.getMessage(), e);
        }
    }

    private String buildHtml(Order order, CompanyDetailsDto company, String campaignTitulo, CertificateConfig cfg) {
        String numero = String.format("N° %06d", order.getId());
        String emision = LocalDateTime.now().format(FECHA);
        String fechaDonacion = order.getOrderDate() != null ? order.getOrderDate().format(FECHA) : emision;
        String monto = formatClp(order.getFinalPrice());

        StringBuilder filas = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem it : order.getItems()) {
                String nombre = StringUtils.hasText(it.getKitNameSnapshot()) ? it.getKitNameSnapshot() : ("Kit #" + it.getKitId());
                filas.append("<tr><td style='padding:4px 8px;border-bottom:1px solid #eee;'>")
                        .append(esc(nombre))
                        .append("</td><td style='padding:4px 8px;border-bottom:1px solid #eee;text-align:center;'>")
                        .append(it.getQuantity() == null ? 1 : it.getQuantity())
                        .append("</td></tr>");
            }
        }

        String clausula = cfg != null && StringUtils.hasText(cfg.getClausulaLegal()) ? cfg.getClausulaLegal()
                : "El presente certificado acredita la donación efectuada a través de Donatech.";
        String repNombre = cfg != null && StringUtils.hasText(cfg.getRepresentanteNombre()) ? cfg.getRepresentanteNombre() : "Donatech";
        String repCargo = cfg != null && StringUtils.hasText(cfg.getRepresentanteCargo()) ? cfg.getRepresentanteCargo() : "Representante Legal";
        String pie = cfg != null && StringUtils.hasText(cfg.getPie()) ? cfg.getPie() : "Donatech — Hub Logístico de Donaciones";

        String razonSocial = company != null && StringUtils.hasText(company.getRazonSocial()) ? company.getRazonSocial() : "—";
        String rut = company != null && StringUtils.hasText(company.getRut()) ? company.getRut() : "—";
        String giro = company != null && StringUtils.hasText(company.getGiro()) ? company.getGiro() : "—";
        String domicilio = company != null && StringUtils.hasText(company.getDireccionLegal()) ? company.getDireccionLegal() : "—";
        String campania = StringUtils.hasText(campaignTitulo) ? campaignTitulo : ("Campaña #" + (order.getCampaignId() == null ? "" : order.getCampaignId()));

        return """
                <html><head><meta charset="UTF-8"/>
                <style>
                  body { font-family: sans-serif; color:#1f2937; font-size:12px; }
                  .wrap { border:2px solid #1565c0; padding:28px; }
                  h1 { color:#1565c0; font-size:22px; margin:0 0 4px; }
                  .sub { color:#6b7280; font-size:11px; margin:0 0 18px; }
                  .row { margin:6px 0; }
                  .label { color:#6b7280; }
                  .val { font-weight:bold; }
                  .monto { font-size:18px; color:#1565c0; font-weight:bold; }
                  table { width:100%; border-collapse:collapse; margin:10px 0; }
                  .clausula { font-size:11px; color:#374151; margin:16px 0; line-height:1.5; }
                  .firma { margin-top:48px; text-align:center; }
                  .firma .line { border-top:1px solid #374151; width:240px; margin:0 auto 4px; }
                  .pie { margin-top:24px; font-size:10px; color:#9ca3af; text-align:center; }
                </style></head>
                <body><div class="wrap">
                  <h1>Certificado de Donación</h1>
                  <p class="sub">%NUMERO% · Emitido el %EMISION%</p>

                  <div class="row"><span class="label">Razón social: </span><span class="val">%RAZON%</span></div>
                  <div class="row"><span class="label">RUT: </span><span class="val">%RUT%</span></div>
                  <div class="row"><span class="label">Giro: </span><span class="val">%GIRO%</span></div>
                  <div class="row"><span class="label">Domicilio: </span><span class="val">%DOMICILIO%</span></div>
                  <hr style="border:none;border-top:1px solid #e5e7eb;margin:16px 0;"/>
                  <div class="row"><span class="label">Fecha de la donación: </span><span class="val">%FECHA_DON%</span></div>
                  <div class="row"><span class="label">Destino (campaña): </span><span class="val">%CAMPANIA%</span></div>
                  <div class="row"><span class="label">Monto donado: </span><span class="monto">%MONTO%</span></div>

                  <table>
                    <tr><th style="text-align:left;border-bottom:2px solid #1565c0;padding:4px 8px;">Kit donado</th>
                        <th style="text-align:center;border-bottom:2px solid #1565c0;padding:4px 8px;">Cantidad</th></tr>
                    %FILAS%
                  </table>

                  <p class="clausula">%CLAUSULA%</p>

                  <div class="firma">
                    <div class="line"></div>
                    <div class="val">%REP_NOMBRE%</div>
                    <div class="label">%REP_CARGO%</div>
                  </div>

                  <p class="pie">%PIE%</p>
                </div></body></html>
                """
                .replace("%NUMERO%", esc(numero))
                .replace("%EMISION%", esc(emision))
                .replace("%RAZON%", esc(razonSocial))
                .replace("%RUT%", esc(rut))
                .replace("%GIRO%", esc(giro))
                .replace("%DOMICILIO%", esc(domicilio))
                .replace("%FECHA_DON%", esc(fechaDonacion))
                .replace("%CAMPANIA%", esc(campania))
                .replace("%MONTO%", esc(monto))
                .replace("%FILAS%", filas.toString())
                .replace("%CLAUSULA%", esc(clausula))
                .replace("%REP_NOMBRE%", esc(repNombre))
                .replace("%REP_CARGO%", esc(repCargo))
                .replace("%PIE%", esc(pie));
    }

    private String formatClp(Integer value) {
        int v = value == null ? 0 : value;
        return "$" + String.format("%,d", v).replace(',', '.') + " CLP";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}
