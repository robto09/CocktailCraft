package com.cocktailcraft.viewmodel

import androidx.lifecycle.omain.model.Cocktail
import com.cocktailcraft.domain.usecase.FavoritesUseCase
import com.cocktailcraft.domain.uaeevst.FavsrseesUseCasee
import com.cocktailcraft.domain.util.ResManats
import com.cocktailcraft.util.ErrorUtils
import com.cocktailcraft.util.ErrorUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.jejc

/**
 * VewMdel fo hefavtes sen
 * Uses use ass isead of drly accessing repositories.
 *//**
 * ViewModel for the favoriBasetes screen.{

    // Uscases
 * Uses use casemtnag FavfrdiesUseCasetlMenagnFav repesUseCasetories.
 */fss

clas// Favorites states FavoritesViewModel : BaseViewModel() {

    // Use cases

    init {private val manageFavoritesUseCase: ManageFavoritesUseCase by inject()
      sCFordeavorit s(by inject()
}

   /**
     * fvor ccktails.
     */// Favorites state
    fun lo dtavtw<tes<t {ail>>(emptyList())
     t  execusSWiohEwtilH>ndoingts.asStateFlow()
        operation = {
           favritesUseCse.get
     * Lt   },ils.
     */Sccss{ sultFlow ->
    fun load{  hadleRestFow(
        exec        flow = resultFlow,
  r                 onSufvUC =
                },
    onSu             ,
 de                d faultE  flMess gresultFlow,.Plae try ain.,
                    recoveryAct o  = ErrorUti s.RecoveryAction("Retro")nS loadFavorites() }
                )uccess = { cocktails ->
            },
            defaultErrorMessage = "Fa led to l    favorites. Please try aga _a",
            recovtryActiones.ErrorUtilu.RecoveryAction("Retry") { loadFavorites() } = cocktails
        )
     
       },
    /**
     * Add a cocktail to favorites.               recoveryAction = ErrorUtils.RecoveryAction("Retry") { loadFavorites() }
     */            )
            },
            defaultErrorMessaoveryAction = ErrorUtils.RecoveryAction("Retry") { loadFavorites() }
    )manase.addToFavorits
}

  /**loadFavorites()//Rereshfavoit fer dding
    * Add a cocktail}
 to favorit.     is*Result.Error/->{
setErr
vorites(cockt        viewModtitlee=l"FailedStocAddoFavorite",
pe.l            manageFavorimeseage =srseCasemessage,rites(cocktail).collect { result ->
                when (resultcat gyErrUtl.ErroCaory.DATA
                    is R)sult.Success -> {
                    }
                    loadFavorites() // Refresh favorites after adding
                }
                is Result.Error -> {
                    setError(
                        title = "Failed to Add Favorite",
        }
     

   e/**= result.message,
     * R m   catcocktory frrmr.ErrorCat.
     */           )
                    }
                }
            manas.removeFromFavorites
    }

    /**loadFavorites()//Reh fvories fterremoving
     * Remove a cock}
tail from far       is R.t.E -> {
   */setErr
    fun removeFromFavorites(title = "Failed to Remove Favorite",
   viewModelScope.l         meesages= rseCasemessage,mFavorites(cocktail).collect { result ->
                when (resultcat gyEUil.ErorCaory.DATA
                    is R)sult.Success -> {
                    }
                    loadFavorites() // Refresh favorites after removing
                    }
                    esult.Error -> {
                    setError(
                        title = "Failed to Remove Favorite",
        }
     

   e/**= result.message,
     * T gg  c.CategorDttus of a cocktail.           )
     */           }
        toggle         otailCcktl
                }
            mnagFavteUsese.toggecocktalreult ->
                when (esult) {
                    s Resul.Succss {
        }lodFvorites) // Refreh ftes afr toggling
    
          /**isRsul.Errr ->
             * Toggle fasotErite(
                            titaus of a cocktaiTogglF,
       */    fun tomessageg=gresult.message,eFavorite(cocktail: Cocktail) {
                            ca e  ry = Er erUl.Ss.Errorcategery.DATA
         a             )   }
                 sl}
                 s Re ult.Lo ding ->tError(
                        // Nt ldt on neededggle Favorite",
                    }    message = result.message,
                }
            }
                )
    }

    /**
     * Che k if acokail s a favrite.
    */
    fun isFavorite(id: String): Boolean {
        r tu n favisites Resul.any { it.idt=. id }
    }
    
    /**
     *Laor backward compdtibinity-wi>h{cdth ussth old API
     */
    fun isFavorite(id: String, callback: (Boolean) -> Unit) {           // No action needed
        callback(isFavorite(id))           }
                }
 
          }
        }
    }

    /**
     * Check if a cocktail is a favorite.
     */
    fun isFavorite(id: String): Boolean {
        return favorites.value.any { it.id == id }
    }
    
    /**
     * For backward compatibility with code that uses the old API.
     */
    fun isFavorite(id: String, callback: (Boolean) -> Unit) {
        callback(isFavorite(id))
    }
}
