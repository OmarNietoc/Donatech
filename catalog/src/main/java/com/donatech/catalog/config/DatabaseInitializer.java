package com.donatech.catalog.config;

import com.donatech.catalog.model.Category;
import com.donatech.catalog.model.Product;
import com.donatech.catalog.model.Unit;
import com.donatech.catalog.repository.CategoryRepository;
import com.donatech.catalog.repository.ProductRepository;
import com.donatech.catalog.repository.UnitRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.util.Optional;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UnitRepository unitRepository;

    public DatabaseInitializer(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               UnitRepository unitRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("📦 Iniciando carga de insumos humanitarios (Idempotente)...");

        // --- Alimentos ---
        crearProducto("AL001", "Arroz grano largo 5kg",
                "Arroz blanco grano largo, esencial para la alimentación de familias damnificadas. Rendimiento alto, fácil preparación en condiciones de emergencia.",
                3500, null, "alimentos", "fardo");
        crearProducto("AL002", "Lentejas 1kg",
                "Legumbre rica en proteínas y hierro, fundamental en raciones de emergencia. Larga vida útil y alto valor nutricional para personas en situación de catástrofe.",
                1800, null, "alimentos", "kg");
        crearProducto("AL003", "Leche en polvo descremada 400g",
                "Leche en polvo descremada de alta calidad nutricional, prioritaria para niños y adultos mayores en zonas de catástrofe. Sin refrigeración requerida.",
                2900, null, "alimentos", "unidad");
        crearProducto("AL004", "Conservas de atún en agua 170g",
                "Conserva de atún lista para consumo, fuente de proteína de alta calidad. Ideal para raciones de emergencia en zonas sin acceso a cocina.",
                1200, null, "alimentos", "unidad");
        crearProducto("AL005", "Galletas de salvado x200g",
                "Galletas integrales de salvado, fuente de fibra y energía rápida. Empaque resistente a la humedad, apto para condiciones de emergencia y almacenamiento prolongado.",
                900, null, "alimentos", "caja");

        // --- Agua y Líquidos ---
        crearProducto("AG001", "Agua purificada 5 litros",
                "Bidón de agua purificada de 5 litros, libre de contaminantes. Recurso crítico en zonas de catástrofe donde el suministro hídrico ha sido interrumpido.",
                1500, null, "agua_y_liquidos", "unidad");
        crearProducto("AG002", "Pastillas purificadoras de agua x50",
                "Tabletas de purificación de agua para uso en emergencias. Cada pastilla trata hasta 1 litro de agua contaminada, ideales para zonas sin acceso a agua potable.",
                2200, null, "agua_y_liquidos", "unidad");
        crearProducto("AG003", "Suero oral rehidratante x10 sobres",
                "Sobres de sales de rehidratación oral para prevenir deshidratación en damnificados. Especialmente importante para niños y adultos mayores en condiciones de calor extremo.",
                1800, null, "agua_y_liquidos", "caja");

        // --- Medicamentos ---
        crearProducto("ME001", "Paracetamol 500mg x24 comprimidos",
                "Analgésico y antipirético de uso general, esencial en botiquines de emergencia. Para el alivio del dolor y la fiebre en situaciones de catástrofe donde no hay acceso médico inmediato.",
                1200, null, "medicamentos", "caja");
        crearProducto("ME002", "Vendas elásticas 10cm x5 unidades",
                "Vendas elásticas de 10 cm de ancho para inmovilización y compresión de heridas. Componente esencial de botiquines de primera respuesta en zonas de desastre.",
                2500, null, "medicamentos", "caja");
        crearProducto("ME003", "Kit de primeros auxilios básico",
                "Kit completo con gasas, apósitos, esparadrapo, tijeras, guantes y alcohol. Equipamiento mínimo indispensable para atención de heridos leves en zonas de emergencia.",
                8900, null, "medicamentos", "kit");

        // --- Higiene Personal ---
        crearProducto("HG001", "Jabón en barra x3 unidades",
                "Jabón antiséptico en barra para higiene personal en condiciones de emergencia. Previene enfermedades infecciosas en zonas afectadas por catástrofes naturales.",
                1500, null, "higiene_personal", "unidad");
        crearProducto("HG002", "Papel higiénico x12 rollos",
                "Papel higiénico doble hoja, producto básico de higiene para familias damnificadas. Empaque resistente a la humedad para condiciones de almacenamiento en emergencia.",
                4500, null, "higiene_personal", "unidad");
        crearProducto("HG003", "Pañales talla M x30 unidades",
                "Pañales desechables talla M para bebés de 6 a 11 kg. Insumo crítico para familias con infantes en zonas de catástrofe sin acceso a higiene básica.",
                12000, null, "higiene_personal", "unidad");

        // --- Ropa y Abrigo ---
        crearProducto("RO001", "Frazada polar 1.5x2m",
                "Frazada de polar grueso para protección térmica en zonas de catástrofe. Resistente a la humedad, lavable, prioritaria para adultos mayores y niños en albergues de emergencia.",
                9800, null, "ropa_y_abrigo", "unidad");
        crearProducto("RO002", "Carpa familiar 4 personas",
                "Carpa impermeable para 4 personas con piso integrado y ventilación. Refugio temporal esencial para familias que han perdido su vivienda por catástrofe natural.",
                45000, null, "ropa_y_abrigo", "unidad");
        crearProducto("RO003", "Ropa interior adulto kit básico",
                "Kit de ropa interior básica para adulto (3 prendas interiores). Insumo de primera necesidad para personas evacuadas que no pudieron rescatar pertenencias personales.",
                6500, null, "ropa_y_abrigo", "kit");

        // --- Herramientas ---
        crearProducto("HR001", "Linterna LED con pilas incluidas",
                "Linterna de alta luminosidad LED con pilas incluidas, resistente al agua. Herramienta crítica para zonas con corte de suministro eléctrico durante catástrofes.",
                5900, null, "herramientas", "unidad");
        crearProducto("HR002", "Pala de emergencia plegable",
                "Pala plegable de acero resistente para remoción de escombros y habilitación de accesos. Herramienta indispensable para brigadas de voluntarios en zonas de derrumbe.",
                15000, null, "herramientas", "unidad");

        // --- Comunicación ---
        crearProducto("CO001", "Radio AM/FM a pilas",
                "Radio portátil AM/FM que funciona con pilas estándar, para recepción de alertas y comunicados de autoridades en zonas sin electricidad durante emergencias.",
                8500, null, "comunicacion", "unidad");

        System.out.println("✅ Carga de insumos humanitarios finalizada.");
    }

    private void crearProducto(String id, String nombre, String descripcion, Integer precio,
                               String imageName, String categoriaNombre, String unidadNombre, String oferta) {
        crearProducto(id, nombre, descripcion, precio, imageName, categoriaNombre, unidadNombre);
    }

    private void crearProducto(String id, String nombre, String descripcion, Integer precio,
                               String imageName, String categoriaNombre, String unidadNombre) {

        // Si el producto ya existe, no hacer nada (idempotente sin actualizar registros existentes)
        if (productRepository.existsById(id)) {
            // System.out.println("⏭️ Producto " + nombre + " ya existe. Saltando...");
            return;
        }

        try {
            // 2. Buscar Entidades Relacionadas (Categoria y Unidad)
            Optional<Category> categoriaOpt = categoryRepository.findByName(categoriaNombre);
            if (categoriaOpt.isEmpty()) {
                System.err.println("⚠️ Categoría no encontrada: " + categoriaNombre);
                return;
            }

            Optional<Unit> unitOpt = unitRepository.findByName(unidadNombre);
            if (unitOpt.isEmpty()) {
                System.err.println("⚠️ Unidad no encontrada: " + unidadNombre);
                return;
            }

            byte[] imageBytes = loadImageFromResources(imageName);

            // 4. Construir y Guardar el Producto
            Product product = Product.builder()
                    .id(id)
                    .nombre(nombre)
                    .descripcion(descripcion)
                    .precio(precio)
                    .stock(100)       // Requisito: Stock 100
                    .stockMinimo(15)  // Requisito: Stock Mínimo 15
                    .activo(1)        // Default activo
                    .categoria(categoriaOpt.get())
                    .unid(unitOpt.get())
                    .imagen(imageBytes)
                    .build();

            productRepository.save(product);
            System.out.println("✅ Producto creado: " + nombre);

        } catch (Exception e) {
            System.err.println("❌ Error crítico creando producto " + nombre + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] loadImageFromResources(String imageName) {
        // Primero busca en la carpeta img/products (donde están las imágenes reales)
        String[] candidatePaths = {"img/products/" + imageName, "img/" + imageName};
        for (String candidatePath : candidatePaths) {
            try {
                ClassPathResource imgFile = new ClassPathResource(candidatePath);
                if (imgFile.exists()) {
                    return StreamUtils.copyToByteArray(imgFile.getInputStream());
                }
            } catch (IOException e) {
                System.err.println("❌ Error leyendo imagen " + imageName + " desde " + candidatePath + ": " + e.getMessage());
            }
        }

        System.err.println("⚠️ Imagen no encontrada en resources/img/ ni img/products/: " + imageName);
        return null;
    }
}
