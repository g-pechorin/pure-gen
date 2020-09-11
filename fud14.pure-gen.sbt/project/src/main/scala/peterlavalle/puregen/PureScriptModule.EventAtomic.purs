-- <@ir/>
foreign import data FSFU<@name/> :: Type
foreign import fsfn<@name/> :: <@args/>Effect FSFU<@name/>
foreign import fsfi<@name/> :: (<@kind/> -> Maybe <@kind/>) -> (Maybe <@kind/>) -> FSFU<@name/> -> Effect (Maybe <@kind/>)

open<@name/> :: <@args/>Effect (SF Unit (Maybe <@kind/>))
open<@name/><@pass/> = do
  p <- fsfn<@name/><@pass/>
  pure $ (consta p) >>>> (Lift (fsfi<@name/> Just Nothing))


