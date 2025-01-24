# 📚 moodleAnd
<p align="center">
  <img src="https://i.imgur.com/csdVnHi.png">
</p>

📱 Cliente Android para **Moodle Centros** programado en **Java,** con funciones útiles como recordar tu IdEA y contraseña (útil cuando caduca la sesión del CAS de la Consejería).

⚠️ Por favor, ten en cuenta que este proyecto es un **proyecto en desarrollo** y que las sources actuales son parte de un proceso transitorio hacia las sources del cliente oficial de Moodle para Android e iOS.

## 🏁 Metas/por hacer
🎯 Objetivos a corto, medio y largo plazo que me gustaría ver concretados para moodleAnd.
- [x] ▶️ Commit inicial, proyecto compilable y funcional.
- [x] 🏙️ Selector de provincia para evitar confusión.
- [x] 🪪 Autologin con IdEA y contraseña. Prevención de caducidad de sesión.
- [x] 🗣️ Localización en Inglés de los menús de configuración inicial y (futuros) menú de configuración.
- [x] 🔑 Uso de `EncryptedSharedPreferences` como medio de guardado de decisiones de configuración inicial y autologin (para evitar acceso externo).
- [ ] 🛠️ Menú de configuración basado en `PreferenceScreen` para realizar acciones como cerrar sesión, cambiar de instancia (provincia), etc.
- [ ] 📠 Modernizar las `APIs` deprecadas en versiones actuales de Android.
- [ ] 🔔 Notificaciones push (recordatorios de tareas a entregar, nuevas tareas y nuevos contenidos) (implementación poco probable debido a la naturaleza web del cliente)

## ⚙️ Compilación
🔧 Al ser un proyecto creado en la IDE **Android Studio**, deberías ser capaz de abrir el proyecto directamente en cualquier versión moderna de la IDE.

Para lograr esto, haz un **fork** de este repositorio:
```
mkdir moodleAnd
git clone https://github.com/imhexp/moodleAnd.git
```

Ahora, en **Android Studio,** abre el repositorio clonado `/moodleAnd/src`. 

Podrás **compilar** el proyecto, **modificarlo** a tu gusto, y, si quieres, incluso hacer una **pull request.** ¡Cualquier ayuda es bienvenida!

## ⚖️ Licencia y créditos
🫂 Proyecto realizado por `hexp` y por `ti`! Puedes **contribuir** forkeando el proyecto y haciendo una **pull request.**

<p xmlns:cc="http://creativecommons.org/ns#">⚖️ This work is licensed under <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/?ref=chooser-v1" target="_blank" rel="license noopener noreferrer" style="display:inline-block;">CC BY-NC-SA 4.0<img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/cc.svg?ref=chooser-v1" alt=""><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/by.svg?ref=chooser-v1" alt=""><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/nc.svg?ref=chooser-v1" alt=""><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/sa.svg?ref=chooser-v1" alt=""></a></p>