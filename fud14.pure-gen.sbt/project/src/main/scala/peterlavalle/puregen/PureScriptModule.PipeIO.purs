-- <@ir/>


data <@name/>E
  <@newe/>

data <@name/>S
  <@news/>

foreign import data <@name/> :: Type
foreign import fsfn<@name/> :: <@args/>Effect <@name/>

foreign import fsfi<@name/> :: (Maybe <@name/>E) -> <@alts/><@name/> -> Effect (Maybe <@name/>E)

foreign import fsfo<@name/>_<@send/>Effect Unit

-- open<@name/> :: <@args/>Effect (SF <@name/>S (Maybe <@name/>E))
open<@name/> :: <@args/>Effect (SF <@name/>S (Maybe <@name/>E))
open<@name/><@pass/> = do
  p <- fsfn<@name/><@pass/>

  let s = Lift $ fsfo p

  let e = Lift $ fsfi p

  -- pure $ s >>>> e
  pure $ Pipe {take: s, send: e}
  where
    fsfi :: <@name/> -> Unit -> Effect (Maybe <@name/>E)
    fsfi p _ = fsfi<@name/> Nothing<@just/> p

    fsfo :: <@name/> -> <@name/>S -> Effect Unit
    fsfo <@case/>
