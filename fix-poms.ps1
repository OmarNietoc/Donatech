$parentXml = @"
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
"@

$depManagement = @"
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2023.0.1</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
"@

$dirs = "api-gateway", "auth", "catalog", "discovery-server", "order", "supports", "users"

foreach ($d in $dirs) {
    $file = "$d\pom.xml"
    if (Test-Path $file) {
        $content = Get-Content $file -Raw
        
        # Replace the parent block
        $content = $content -replace '(?s)<parent>.*?<\/parent>', $parentXml
        
        # Check if dependencyManagement exists, if not, inject it before <build>
        if ($content -notmatch '<dependencyManagement>') {
            $content = $content -replace '</dependencies>', "</dependencies>`n$depManagement"
        }
        
        Set-Content -Path $file -Value $content
        Write-Host "Updated $file"
    }
}
