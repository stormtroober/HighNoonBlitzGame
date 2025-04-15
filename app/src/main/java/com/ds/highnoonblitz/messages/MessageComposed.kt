package com.ds.highnoonblitz.messages
import org.json.JSONArray
import org.json.JSONObject

class MessageComposed private constructor(
    private val message: JSONObject,
    private val purpose: Int,
) {
    companion object {
        class MessageBuilder {
            private var message = JSONObject()
            private var purpose: Int = Purposes.NETWORK_ACK.value

            fun addMessageParameter(
                key: String,
                value: Any,
            ): MessageBuilder {
                when (value) {
                    is String -> message.put(key, value)
                    is Boolean -> message.put(key, value)
                    is JSONObject -> {
                        if (message.has(key)) {
                            val existingObject = message.getJSONObject(key)
                            val keys = value.keys()
                            while (keys.hasNext()) {
                                val nextKey = keys.next()
                                existingObject.put(nextKey, value.get(nextKey))
                            }
                        } else {
                            message.put(key, value)
                        }
                    }
                    is List<*> -> {
                        if (value.all { it is String }) {
                            val jsonArray = JSONArray(value)
                            message.put(key, jsonArray)
                        } else {
                            throw IllegalArgumentException("List elements are not all Strings")
                        }
                    }
                    is Int -> message.put(key, value)
                    is Long -> message.put(key, value)
                    else -> throw IllegalArgumentException("Unsupported type: ${value.javaClass.simpleName}")
                }
                return this
            }

            fun addMessage(message: JSONObject): MessageBuilder {
                this.message = message
                return this
            }

            fun setPurpose(purpose: Int): MessageBuilder {
                this.purpose = purpose
                return this
            }

            fun build(): MessageComposed = MessageComposed(message, purpose)

            fun fromString(jsonString: String): MessageBuilder {
                val jsonObject = JSONObject(jsonString)
                this.message = jsonObject.getJSONObject("message")
                this.purpose = jsonObject.getInt("purpose")
                return this
            }
        }
    }

    fun getMessage(): JSONObject = message

    fun getPurpose(): Int = purpose

    override fun toString(): String {
        val jsonObject = JSONObject()
        jsonObject.put("message", message)
        jsonObject.put("purpose", purpose)
        return jsonObject.toString()
    }
}
