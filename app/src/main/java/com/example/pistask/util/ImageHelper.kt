package com.example.pistask.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID

object ImageHelper {

    /**
     * Copie une image depuis un URI content:// OU un chemin absolu
     * dans le dossier interne de l'app pour que l'URI reste valide après redémarrage.
     * Retourne le chemin absolu du fichier copié, ou null en cas d'échec.
     */
    fun saveImageToInternalStorage(context: Context, sourceUri: String): String? {
        return try {
            val dir = File(context.filesDir, "task_images").apply { if (!exists()) mkdirs() }
            val dest = File(dir, "${UUID.randomUUID()}.jpg")

            val inputStream = when {
                // Déjà dans notre stockage interne → rien à faire, on retourne le chemin
                sourceUri.startsWith(context.filesDir.absolutePath) -> return sourceUri

                // Chemin absolu fichier (ex: depuis caméra via MediaStore.insertImage)
                sourceUri.startsWith("/") -> FileInputStream(File(sourceUri))

                // URI content:// (galerie, caméra récente)
                else -> context.contentResolver.openInputStream(Uri.parse(sourceUri))
            } ?: return null

            FileOutputStream(dest).use { out ->
                inputStream.use { it.copyTo(out) }
            }
            dest.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Supprime une image sauvegardée en stockage interne.
     */
    fun deleteImageFromInternalStorage(path: String) {
        try {
            File(path).delete()
        } catch (_: Exception) {}
    }
}
