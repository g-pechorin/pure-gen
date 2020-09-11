-- <@ir/>
foreign import data FSFU<@name/> :: Type
foreign import fsfn<@name/> :: <@args/>Effect FSFU<@name/>
foreign import fsfo<@name/> :: FSFU<@name/> -> <@kind/> -> Effect Unit

open<@name/> :: <@args/>Effect (SF <@kind/> Unit)
open<@name/><@pass/> = do
  p <- fsfn<@name/><@pass/>
  pure $ Lift $ fsfo<@name/> p


