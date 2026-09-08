package com.github.wpm.lean.unicode

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger

/** Loads the bundled abbreviation table once per IDE session. */
@Service(Service.Level.APP)
class AbbreviationService {
    val provider: AbbreviationProvider by lazy { load() }

    private fun load(): AbbreviationProvider {
        val stream = AbbreviationService::class.java.getResourceAsStream(RESOURCE)
        if (stream == null) {
            LOG.error("Missing abbreviation table resource $RESOURCE")
            return AbbreviationProvider(emptyMap())
        }
        val type = object : TypeToken<LinkedHashMap<String, String>>() {}.type
        val table: LinkedHashMap<String, String> = stream.reader(Charsets.UTF_8).use { Gson().fromJson(it, type) }
        LOG.debug("Loaded ${table.size} Lean abbreviations")
        return AbbreviationProvider(table)
    }

    companion object {
        private const val RESOURCE = "/lean/abbreviations.json"
        private val LOG = logger<AbbreviationService>()

        fun getInstance(): AbbreviationService = service()
    }
}
