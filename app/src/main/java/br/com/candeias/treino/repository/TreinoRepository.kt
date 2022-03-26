package br.com.candeias.treino.repository


import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import br.com.candeias.treino.model.Exercicio
import br.com.candeias.treino.model.Treino
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import util.singleton.FireBaseStarangeApi
import util.singleton.FireStoreApi.firebaseFirestore
import java.net.MalformedURLException
import java.net.URL


object TreinoRepository {

    private val treino = "TREINO"
    private val exercicio = "EXERCICIO"

    suspend fun getdata(): List<Treino>? {
        return try {
            var exerciciosL = getExerciciosFromFireStore()
            var treinosl = getTreinosFromFireStore(exerciciosL)
            var retorno = associatTreineExercicio(treinosl as MutableList<Treino>, exerciciosL as MutableList<Exercicio>)
            retorno
        } catch (e: Exception) {
            null
        }
    }


    suspend fun getExerciciosFromFireStore()
            : List<Exercicio>? {
        return try {
            var data = firebaseFirestore?.collection(exercicio).get().await()
            val exerciciosLocal: MutableList<Exercicio> = ArrayList()
            val exeList: MutableList<Exercicio> = ArrayList()
            data.let { result ->
                for (document in result) {
                    val e: Exercicio = fillExercicioInstance(document)
                    if (!exerciciosLocal.contains(e)) {
                        exerciciosLocal.add(e)
                    }
                }
            }
            exerciciosLocal
        } catch (e: Exception) {
            null
        }
    }


    suspend fun getTreinosFromFireStore(execList: List<Exercicio>?): List<Treino> {
        val fireStoredb = Firebase.firestore
        val treinos: MutableList<Treino> = ArrayList()
        if (execList != null) {
            for (exec in execList) {
                val d: DocumentReference? = fireStoredb?.document(exercicio + "/" + exec.id)
                d?.let { fireStoredb?.collection(treino)?.whereArrayContains("exercicios", it) }
                    ?.get()
                    ?.await().let { result ->
                        if (result != null) {
                            for (document in result) {
                                val t = fillTreinoInstance(document)
                                if (!treinos.contains(t)) {
                                    treinos.add(t)
                                }
                                getImagesLikeUrl(t.id, exec)
                            }
                        }
                    }
            }
        }
        return treinos
    }


    fun associatTreineExercicio(
        treinos: MutableList<Treino>,
        exercicios: MutableList<Exercicio>
    ): MutableList<Treino> {

        for (t in treinos) {
            for (e in exercicios) {
                for (d in t.strinExe) {
                    val ls: String = d.getPath()
                    val l = ls.split("/").toTypedArray()
                    val idDtr = l[1]
                    if (e.id.equals(idDtr)) {
                        if (!t?.exercicios.contains(e)) {
                            t.exercicios.add(e)
                        }
                    }
                }
            }
        }
        return treinos
    }


    suspend fun getImagesLikeUrl(idTreino: String, e: Exercicio) {
        val storage: FirebaseStorage? = FireBaseStarangeApi.storage
        val storageRef: StorageReference = storage!!.reference
        val folder: StorageReference = storageRef.child("$idTreino/")
        val file: StorageReference = folder.child(e.id + ".png")
        file.downloadUrl.await().let { uri ->
            try {
                val url = URL(uri.toString())
                e.imagem = url
            } catch (malformedURLException: MalformedURLException) {
                malformedURLException.printStackTrace()
            }
        }

    }






    private fun fillExercicioInstance(d: QueryDocumentSnapshot): Exercicio {
        val e = Exercicio()
        e.id = d.getId()
        e.nome = d.getLong("nome")
        e.observacoes = d.get("observacoes").toString()
        return e
    }

    private fun fillTreinoInstance(d: QueryDocumentSnapshot): Treino {
        val t = Treino()
        t.id = d.getId()
        t.nome = d.getLong("nome")
        t.data = d.get("data") as Timestamp
        t.descricao = d.get("descricao").toString()
        t.nome = d.get("nome") as Long
        t.strinExe = d.get("exercicios") as ArrayList<DocumentReference>

        return t
    }






    /*fun getTreinoout(): MutableList<Treino> {
        val localList: MutableList<Treino> = ArrayList()
        val r: MutableList<Treino> = ArrayList()
        localList.addAll(treinoOut)
        val set = HashSet<Treino>()
        set.addAll(treinoOut)
        r.addAll(set.toMutableList())

        for (t in r) {
            for (e in exercicios) {
                for (d in t.strinExe) {
                    val ls: String = d.getPath()
                    val l = ls.split("/").toTypedArray()
                    val idDtr = l[1]
                    if (e.id.equals(idDtr)) {
                        if (!t?.exercicios.contains(e)) {
                            t.exercicios.add(e)
                        }
                    }
                }
            }
        }
        return r
    }*/



    /* fun getInstancesFromApiFireBase() {
        //val fireStoredb: FirebaseFirestore? = FireStoreApi.firebaseFirestore
        val fireStoredb = Firebase.firestore
        fireStoredb?.collection(exercicio)
            ?.get()?.addOnSuccessListener { result ->
                val exeList: MutableList<Exercicio> = ArrayList()
                for (document in result) {
                    val e: Exercicio = fillExercicioInstance(document)
                    exeList.add(e)
                    if (!exercicios.contains(e)) {
                        exercicios.add(e)
                    }
                    getIsntancesOfTreinoFronApiFireBase(e)
                }
            }
            ?.addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }

    }*/

/*
    private fun getIsntancesOfTreinoFronApiFireBase(exec: Exercicio) {
        //val fireStoredb: FirebaseFirestore? = FireStoreApi.firebaseFirestore
        val fireStoredb = Firebase.firestore
        val d: DocumentReference? = fireStoredb?.document(exercicio + "/" + exec.id)
        d?.let { fireStoredb?.collection(treino)?.whereArrayContains("exercicios", it) }
            ?.get()?.addOnSuccessListener { result ->
                for (document in result) {
                    val t = fillTreinoInstance(document)
                    if (!treinoOut.contains(t)) {
                        treinoOut.add(t)
                    }
                    getUrlImages(t.id, exec)
                }
            }
            ?.addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }*/


    /* private fun getUrlImages(idTreino: String, e: Exercicio) {
         val storage: FirebaseStorage? = FireBaseStarangeApi.storage
         val storageRef: StorageReference = storage!!.getReference()
         val folder: StorageReference = storageRef.child("$idTreino/")
         val file: StorageReference = folder.child(e.id.toString().toString() + ".png")
         file.getDownloadUrl().addOnSuccessListener(OnSuccessListener<Any> { uri ->
             try {
                 val url = URL(uri.toString())
                 e.imagem = url
             } catch (malformedURLException: MalformedURLException) {
                 malformedURLException.printStackTrace()
             }
         }).addOnFailureListener(OnFailureListener { exception ->
             Log.d(
                 "Falha",
                 "in method getUrlImages $exception"
             )
         })
     }
 */


    //return a list of DocumentSnapshot
    /*suspend fun getExerciciosFromFireStore()
            : List<DocumentSnapshot>?{
        return try{

            var data=firebaseFirestore?.collection(exercicio).get().await()
            val documentList: MutableList<DocumentSnapshot> = ArrayList()
            data.let { result ->
                for (docoment in result){
                    documentList.add(docoment)
                }
            }
            documentList
        }catch (e : Exception){
            null
        }
    }*/


    //returns a DocumentSnapshot
    /*  suspend fun olhardata():DocumentSnapshot?{
           return try{
               val data=firebaseFirestore?.collection(exercicio).document().get().await()
              data
          }catch (e : Exception){
              null
          }
      }*/

    /* fun putTreino(t: Treino, key: String) {
         val fireStoredb: FirebaseFirestore? = FireStoreApi.Companion.getFirebaseFirestore()
         // Create a new user with a first and last name
         val treino: MutableMap<String, Any> = HashMap()
         treino["nome"] = t.nome!!
         treino["descricao"] = t.descricao!!
         treino["data"] = t.data!!
         val l: MutableList<String> = ArrayList()
         for (e in t.exercicios) {
             l.add(this.treino + "/" + e.id)
         }
         treino["exercicios"] = l
         fireStoredb?.collection(this.treino)?
             .add(treino)
             .addOnSuccessListener(OnSuccessListener<Any> { documentReference ->
                 Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference)
                 idMap[key] = documentReference.getId().toString()
                 return@OnSuccessListener
             })
             .addOnFailureListener(OnFailureListener { e ->
                 Log.w(TAG, "Error adding document", e)
                 return@OnFailureListener
             })
     }

     fun putTreinputTreinoCompleto(t: Treino?) {
         val fireStoredb: FirebaseFirestore = FireStoreApi.Companion.getFirebaseFirestore()
         fireStoredb.collection(treino)
             .add(t)
             .addOnSuccessListener(OnSuccessListener<Any> { documentReference ->
                 Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId())
                 return@OnSuccessListener
             })
             .addOnFailureListener(OnFailureListener { e ->
                 Log.w(TAG, "Error adding document", e)
                 return@OnFailureListener
             })
     }

     fun updateTreino(id: String) {
         val fireStoredb: FirebaseFirestore = FireStoreApi.Companion.getFirebaseFirestore()
         // Create a new user with a first and last name
         val treino: MutableMap<String, Any> = HashMap()
         val l: MutableList<String> = ArrayList()
         l.add(exercicio + "/" + id)
         *//*    l.add(this.treino + "/" + "teste");
        l.add(this.treino + "/" + "teste");
        l.add(this.treino + "/" + "teste");*//*treino["exercicios"] = l
        fireStoredb.collection(this.treino).document("OvQMQiHyfAiYS71RAIBM")
            .set(treino, SetOptions.merge())
    }

    fun putExercicio(e: Exercicio) {
        val fireStoredb: FirebaseFirestore = FireStoreApi.Companion.getFirebaseFirestore()
        // Create a new user with a first and last name
        val exercicio: MutableMap<String, Any> = HashMap()
        exercicio["nome"] = e.getNome()
        exercicio["observacoes"] = e.getObservacoes()
        exercicio["imagem"] = e.getImagem()
        fireStoredb.collection(this.exercicio)
            .add(exercicio)
            .addOnSuccessListener(OnSuccessListener<Any> { documentReference ->
                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId())
                return@OnSuccessListener
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                return@OnFailureListener
            })
    }*/


}




