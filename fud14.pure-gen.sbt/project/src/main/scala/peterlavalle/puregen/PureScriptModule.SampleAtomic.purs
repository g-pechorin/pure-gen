-- <@ir/>
foreign import data FSFU<@name/> :: Type
foreign import fsfn<@name/> :: <@args/>Effect FSFU<@name/>
foreign import fsfi<@name/> :: FSFU<@name/> -> Effect <@kind/>

open<@name/> :: <@args/>Effect (SF Unit <@kind/>)
open<@name/><@pass/> = do
  p <- fsfn<@name/><@pass/>
  pure $ (consta p) >>>> (Lift fsfi<@name/>)

