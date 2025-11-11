# 🗑️ Instrucciones para Eliminar JSONs Duplicados

## ✅ ESTADO ACTUAL

Los JSONs han sido copiados exitosamente a:
```
C:/Users/saron/podiGestProyecto/proyecto-podiGest/backend/base_de_datos/
├── usuarios.json
├── usuarioInicioSesion.json
├── citas.json
└── notificaciones.json
```

## 🚨 ARCHIVOS A ELIMINAR MANUALMENTE

### 1️⃣ Elimina TODOS los JSONs de `/src/main/resources/`:
```
C:/Users/saron/podiGestProyecto/proyecto-podiGest/backend/src/main/resources/
├── usuarios.json                    ❌ ELIMINA
├── usuarioInicioSesion.json        ❌ ELIMINA
├── citas.json                       ❌ ELIMINA
└── notificaciones.json              ❌ ELIMINA
```

**Pasos:**
1. Abre: `C:/Users/saron/podiGestProyecto/proyecto-podiGest/backend/src/main/resources/`
2. Selecciona y elimina los 4 archivos JSON arriba listados
3. Deja `application.properties` (NO eliminar)

---

### 2️⃣ Elimina TODOS los JSONs de `/base_de_datos/` (raíz):
```
C:/Users/saron/podiGestProyecto/base_de_datos/
├── usuarios.json           ❌ ELIMINA
├── citas.json              ❌ ELIMINA
└── notificaciones.json     ❌ ELIMINA
```

**Pasos:**
1. Abre: `C:/Users/saron/podiGestProyecto/base_de_datos/`
2. Selecciona y elimina los 3 archivos JSON arriba listados
3. Si la carpeta queda vacía, puedes eliminarla también (opcional)

---

### 3️⃣ Elimina la carpeta `/proyecto-podiGest/base_de_datos/` si existe:
```
C:/Users/saron/podiGestProyecto/proyecto-podiGest/base_de_datos/
```

**Nota:** Esta carpeta se creó por error en pasos anteriores. Si existe, elimina toda la carpeta.

---

## 📋 RESUMEN DE LO QUE QUEDARÁ

Después de eliminar, tu estructura será:

```
proyecto-podiGest/backend/
├── base_de_datos/              ✅ MANTÉN (al nivel del pom.xml)
│   ├── usuarios.json
│   ├── usuarioInicioSesion.json
│   ├── citas.json
│   └── notificaciones.json
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/podiGest/backend/
│       │       ├── service/
│       │       │   ├── PathConfigService.java
│       │       │   ├── CitasService.java
│       │       │   ├── NotificacionService.java
│       │       │   └── ...
│       │       └── ...
│       └── resources/
│           ├── application.properties      ✅ MANTÉN
│           └── (SIN JSONs)
└── ...
```

---

## ✅ PRÓXIMO PASO

Después de eliminar todos los duplicados, compila el proyecto:

```bash
cd C:/Users/saron/podiGestProyecto/proyecto-podiGest/backend
mvnw clean install
mvnw spring-boot:run
```

---

## 📝 NOTAS IMPORTANTES

- ✅ Los servicios ahora cargan desde `base_de_datos/` automáticamente
- ✅ Los datos se guardan en `{user.home}/podiGest_data/` (portátil)
- ✅ No hay duplicación de datos
- ✅ El proyecto funciona en cualquier computadora

¡Haz la limpieza manual y listo! 🎉