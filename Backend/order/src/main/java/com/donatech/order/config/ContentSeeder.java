package com.donatech.order.config;

import com.donatech.order.model.CertificateConfig;
import com.donatech.order.model.SiteDocument;
import com.donatech.order.repository.CertificateConfigRepository;
import com.donatech.order.repository.SiteDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Siembra contenido base de documentos legales y textos del certificado si aún no existen.
// Idempotente: no sobrescribe lo que el admin haya editado.
@Component
@RequiredArgsConstructor
public class ContentSeeder implements CommandLineRunner {

    private final SiteDocumentRepository documentRepository;
    private final CertificateConfigRepository certificateConfigRepository;

    @Override
    public void run(String... args) {
        if (!documentRepository.existsById("TERMS")) {
            documentRepository.save(SiteDocument.builder()
                    .slug("TERMS")
                    .titulo("Términos y Condiciones")
                    .contenido(TERMS_MD)
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        if (!documentRepository.existsById("PRIVACY")) {
            documentRepository.save(SiteDocument.builder()
                    .slug("PRIVACY")
                    .titulo("Política de Privacidad")
                    .contenido(PRIVACY_MD)
                    .updatedAt(LocalDateTime.now())
                    .build());
        }
        if (!certificateConfigRepository.existsById(1L)) {
            certificateConfigRepository.save(CertificateConfig.builder()
                    .id(1L)
                    .clausulaLegal("El presente certificado se emite para acreditar la donación efectuada a "
                            + "través de la plataforma Donatech, para los fines que el donante estime pertinentes "
                            + "conforme a la legislación chilena vigente sobre donaciones. Documento de carácter "
                            + "informativo; no reemplaza los certificados tributarios oficiales cuando estos sean exigibles.")
                    .representanteNombre("Donatech SpA")
                    .representanteCargo("Representante Legal")
                    .pie("Donatech — Hub Logístico de Donaciones · Este documento fue generado electrónicamente.")
                    .build());
        }
    }

    private static final String TERMS_MD = """
            # Términos y Condiciones

            Última actualización: junio de 2026.

            Bienvenido a **Donatech**, plataforma que centraliza la logística de donaciones para
            situaciones de catástrofe en Chile. Al crear una cuenta o utilizar la plataforma, aceptas
            estos Términos y Condiciones.

            ## 1. Objeto
            Donatech conecta a **donantes**, **empresas/organizaciones**, **beneficiarios** y
            **voluntarios**, facilitando la creación de campañas, la realización de donaciones y el
            seguimiento de su entrega.

            ## 2. Registro y cuentas
            - El usuario debe entregar información veraz y mantenerla actualizada.
            - Las cuentas de beneficiarios y empresas requieren validación por parte de un administrador.
            - El usuario es responsable de la confidencialidad de sus credenciales.

            ## 3. Donaciones
            - Las donaciones se realizan mediante transferencia y requieren la carga de un comprobante.
            - Una donación se considera confirmada una vez validado el pago por el equipo de Donatech.
            - El donante puede cancelar su donación mientras esta no haya sido validada.

            ## 4. Certificado de donación (empresas)
            Las empresas donantes podrán descargar un **certificado de donación** una vez validado el
            pago, con sus datos (razón social, RUT, domicilio) y el monto donado, para los fines que
            estimen pertinentes conforme a la legislación chilena.

            ## 5. Responsabilidades
            - Donatech actúa como intermediario logístico y no garantiza resultados específicos de las campañas.
            - Los usuarios se comprometen a usar la plataforma de buena fe y conforme a la ley.

            ## 6. Modificaciones
            Donatech podrá actualizar estos Términos. Los cambios se publicarán en esta misma página.

            ## 7. Legislación aplicable
            Estos Términos se rigen por las leyes de la República de Chile.
            """;

    private static final String PRIVACY_MD = """
            # Política de Privacidad

            Última actualización: junio de 2026.

            Esta Política describe cómo **Donatech** trata los datos personales de sus usuarios, en
            conformidad con la **Ley N° 19.628 sobre Protección de la Vida Privada**.

            ## 1. Datos que recopilamos
            - **Identificación**: nombre, apellido, RUT (beneficiarios/empresas), correo y teléfono.
            - **Ubicación**: región, comuna y dirección de entrega cuando corresponde.
            - **Transaccionales**: donaciones, comprobantes de transferencia y comprobantes de entrega.

            ## 2. Finalidad del tratamiento
            Los datos se utilizan para gestionar cuentas, procesar y validar donaciones, coordinar la
            logística de entrega, emitir certificados y comunicar el estado de las donaciones.

            ## 3. Comunicación de datos
            Compartimos datos solo con las partes involucradas en una donación (donante, beneficiario,
            voluntario y administración) y en la medida necesaria para cumplir la finalidad descrita.
            No vendemos datos personales a terceros.

            ## 4. Conservación y seguridad
            Conservamos los datos mientras la cuenta esté activa o sea necesario para los fines indicados,
            aplicando medidas razonables de seguridad para protegerlos.

            ## 5. Derechos del titular
            Conforme a la Ley N° 19.628, puedes solicitar **acceso, rectificación, cancelación u
            oposición** respecto de tus datos personales escribiendo a nuestro canal de soporte.

            ## 6. Cambios a esta Política
            Podremos actualizar esta Política; los cambios se publicarán en esta misma página.
            """;
}
